package com.fintech.Exam.service;


import com.fintech.Exam.domain.*;
import com.fintech.Exam.dto.*;
import com.fintech.Exam.exception.BadRequestException;
import com.fintech.Exam.exception.ResourceNotFoundException;
import com.fintech.Exam.repository.ApplicationRepository;
import com.fintech.Exam.repository.WalletRepository;
import com.fintech.Exam.repository.WalletStatementRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletStatementRepository walletStatementRepository;
    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "recentTransactions";
    private static final String CACHE_KEY = "latestList";

    public WalletService(WalletRepository walletRepository, WalletStatementRepository walletStatementRepository, ApplicationRepository applicationRepository, ModelMapper modelMapper, CacheManager cacheManager) {
        this.walletRepository = walletRepository;
        this.walletStatementRepository = walletStatementRepository;
        this.applicationRepository = applicationRepository;
        this.modelMapper = modelMapper;
        this.cacheManager = cacheManager;
    }

    @Transactional
    @CachePut(value = "transactions", key = "#transaction.id")
    public WalletBalanceResponse deposit(WalletOperationRequest request, String performedBy) {
        return performOperation(request, performedBy, WalletTransactionType.DEPOSIT);
    }

    @Transactional
    @CachePut(value = "transactions", key = "#transaction.id")
    public WalletBalanceResponse withdraw(WalletOperationRequest request, String performedBy) {
        return performOperation(request, performedBy, WalletTransactionType.WITHDRAWAL);
    }

    @Transactional
    @CachePut(value = "transactions", key = "#transaction.id")
    public TransferResponse transfer(TransferRequest request, String performedBy) {
        String fromAccountNumber = request.fromAccountNumber().trim();
        String toAccountNumber = request.toAccountNumber().trim();
        validateDifferentAccounts(fromAccountNumber, toAccountNumber);

        Wallet senderWallet = getOrCreateWallet(fromAccountNumber);
        Wallet recipientWallet = getOrCreateWallet(toAccountNumber);
        BigDecimal senderBalanceBefore = senderWallet.getBalance();
        BigDecimal recipientBalanceBefore = recipientWallet.getBalance();
        BigDecimal senderBalanceAfter = subtractAmount(senderBalanceBefore, request.amount());
        BigDecimal recipientBalanceAfter = recipientBalanceBefore.add(request.amount());
        LocalDateTime now = LocalDateTime.now();
        String transferReference = createReference("TRF");

        updateBalance(senderWallet, senderBalanceAfter, now);
        updateBalance(recipientWallet, recipientBalanceAfter, now);
        Wallet savedSenderWallet = walletRepository.save(senderWallet);
        Wallet savedRecipientWallet = walletRepository.save(recipientWallet);

        String narration = request.narration().trim();
        walletStatementRepository.save(createStatement(
                savedSenderWallet,
                createReference("WLT"),
                transferReference,
                WalletTransactionType.WITHDRAWAL,
                request.amount(),
                senderBalanceBefore,
                senderBalanceAfter,
                "Transfer to " + toAccountNumber + ": " + narration,
                performedBy,
                now));
        walletStatementRepository.save(createStatement(
                savedRecipientWallet,
                createReference("WLT"),
                transferReference,
                WalletTransactionType.DEPOSIT,
                request.amount(),
                recipientBalanceBefore,
                recipientBalanceAfter,
                "Transfer from " + fromAccountNumber + ": " + narration,
                performedBy,
                now));

        return new TransferResponse(
                transferReference,
                fromAccountNumber,
                toAccountNumber,
                request.amount(),
                toBalanceResponse(savedSenderWallet),
                toBalanceResponse(savedRecipientWallet),
                "Transfer completed successfully",
                now
        );
    }

    @Transactional
    public WalletBalanceResponse getBalance(String debitParty) {
        Wallet wallet = getOrCreateWallet(debitParty.trim());
        return toBalanceResponse(wallet);
    }

    public List<WalletBalanceResponse> searchTransaction(Optional<String> debitParty) {
        return walletRepository.searchTransaction(
                        debitParty.orElse(null)
                )
                .stream()
                .map(p -> modelMapper.map(p, WalletBalanceResponse.class))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getRecentTransactions() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            ConcurrentLinkedDeque<Transaction> list = cache.get(CACHE_KEY, ConcurrentLinkedDeque.class);
            if (list != null) {
                return new ArrayList<>(list);
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public void addTransactionToCache(Transaction newTransaction) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            ConcurrentLinkedDeque<Transaction> list = cache.get(CACHE_KEY, ConcurrentLinkedDeque.class);
            if (list == null) {
                list = new ConcurrentLinkedDeque<>();
            }

            list.addFirst(newTransaction);

            while (list.size() > 20) {
                list.removeLast();
            }

            cache.put(CACHE_KEY, list);
        }
    }

    private WalletBalanceResponse performOperation(WalletOperationRequest request,
                                                   String performedBy,
                                                   WalletTransactionType type) {
        Wallet wallet = getOrCreateWallet(request.debitParty().trim());
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = calculateNewBalance(balanceBefore, request.amount(), type);

        LocalDateTime now = LocalDateTime.now();
        updateBalance(wallet, balanceAfter, now);
        Wallet savedWallet = walletRepository.save(wallet);
        walletStatementRepository.save(createStatement(
                savedWallet,
                createReference("WLT"),
                null,
                type,
                request.amount(),
                balanceBefore,
                balanceAfter,
                "deposited",
                performedBy,
                now));

        return toBalanceResponse(savedWallet);
    }

    private Wallet getOrCreateWallet(String debitParty) {
        return walletRepository.findByDebitParty(debitParty)
                .orElseGet(() -> createWalletFromApplication(debitParty));
    }

    private Wallet createWalletFromApplication(String debitParty) {
        Application application = applicationRepository.findByDebitParty(debitParty)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found for debitParty: " + debitParty));
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BadRequestException("Wallet is available only for a SUBMITTED account application");
        }

        return walletRepository.save(Wallet.create(application, LocalDateTime.now()));
    }

    private void validateDifferentAccounts(String fromAccountNumber, String toAccountNumber) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BadRequestException("Source and destination wallets must be different");
        }
    }

    private BigDecimal subtractAmount(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }
        return balance.subtract(amount);
    }

    private void updateBalance(Wallet wallet, BigDecimal balance, LocalDateTime updatedAt) {
        wallet.setBalance(balance);
        wallet.setUpdatedAt(updatedAt);
    }

    private BigDecimal calculateNewBalance(BigDecimal balance,
                                           BigDecimal amount,
                                           WalletTransactionType type) {
        if (type == WalletTransactionType.DEPOSIT) {
            return balance.add(amount);
        }
        return subtractAmount(balance, amount);
    }

    private WalletStatement createStatement(Wallet wallet,
                                            String reference,
                                            String transferReference,
                                            WalletTransactionType type,
                                            BigDecimal amount,
                                            BigDecimal balanceBefore,
                                            BigDecimal balanceAfter,
                                            String narration,
                                            String performedBy,
                                            LocalDateTime createdAt) {
        WalletStatement statement = new WalletStatement();
        statement.setReference(reference);
        statement.setTransferReference(transferReference);
        statement.setDebitParty(wallet.getDebitParty());
        statement.setTransactionType(type);
        statement.setAmount(amount);
        statement.setBalanceBefore(balanceBefore);
        statement.setBalanceAfter(balanceAfter);
        statement.setNarration(narration);
        statement.setPerformedBy(performedBy);
        statement.setCreatedAt(createdAt);
        return statement;
    }

    private String createReference(String prefix) {
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return prefix + "-" + uniquePart;
    }

    private WalletBalanceResponse toBalanceResponse(Wallet wallet) {
        return new WalletBalanceResponse(
                wallet.getDebitParty(),
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }

}
