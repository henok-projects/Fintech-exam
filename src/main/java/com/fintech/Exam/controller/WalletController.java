package com.fintech.Exam.controller;

import com.fintech.Exam.domain.Transaction;
import com.fintech.Exam.dto.*;
import com.fintech.Exam.service.WalletService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletBalanceResponse deposit(@Valid @RequestBody WalletOperationRequest request,
                                         Authentication authentication) {
        return walletService.deposit(request, authentication.getName());
    }

    @PostMapping("/withdraw")
    public WalletBalanceResponse withdraw(@Valid @RequestBody WalletOperationRequest request,
                                          Authentication authentication) {
        return walletService.withdraw(request, authentication.getName());
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request,
                                     Authentication authentication) {
        return walletService.transfer(request, authentication.getName());
    }

    @GetMapping("/{debitParty}/balance")
    public WalletBalanceResponse getBalance(@PathVariable @NotBlank String debitParty) {
        return walletService.getBalance(debitParty);
    }

    @GetMapping("/search")
    public ResponseEntity<MessageDTO> searchTransaction(
            @Parameter(description = "First name") @RequestParam Optional<String> debitParty
    ) {

        log.info("[searchPatients] Searching transaction with filter: debitParty={}",
                debitParty);

        List<WalletBalanceResponse> results = walletService.searchTransaction(debitParty);
        return ResponseEntity.ok(MessageDTO.success("Patient search completed successfully", results));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        List<Transaction> recent = walletService.getRecentTransactions();
        return ResponseEntity.ok(recent);
    }

}
