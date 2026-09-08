package com.fintech.Exam.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String transferReference,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        WalletBalanceResponse senderWallet,
        WalletBalanceResponse recipientWallet,
        String message,
        LocalDateTime createdAt
) {
}
