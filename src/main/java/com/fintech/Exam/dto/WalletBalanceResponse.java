package com.fintech.Exam.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletBalanceResponse(
        String debitParty,
        BigDecimal balance,
        LocalDateTime createdAt
) {
}
