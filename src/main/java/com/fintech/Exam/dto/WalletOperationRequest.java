package com.fintech.Exam.dto;

import com.fintech.Exam.domain.WalletTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletOperationRequest(
        @NotBlank WalletTransactionType transactionType,
        @NotBlank String currency,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String debitParty,
        String creditParty,
        String accountReference,
        String category,
        String status,
        LocalDateTime timestamp
) {
}