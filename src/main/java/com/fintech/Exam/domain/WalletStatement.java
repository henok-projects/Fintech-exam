package com.fintech.Exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_wallet_statement")
@Getter
@Setter
public class WalletStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    @Column(name = "debit_party")
    private String debitParty;
    @Column(name = "transfer_reference")
    private String transferReference;
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private WalletTransactionType transactionType;
    private BigDecimal amount;
    @Column(name = "balance_before")
    private BigDecimal balanceBefore;
    @Column(name = "balance_after")
    private BigDecimal balanceAfter;
    private String narration;
    @Column(name = "performed_by")
    private String performedBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
