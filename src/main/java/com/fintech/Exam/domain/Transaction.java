package com.fintech.Exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_transacion_summary")
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @NotBlank
    String currency;

    private String transactionType;

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount;

    @Column(name = "debit_party")
    private String debitParty;

    @Column(name = "credit_party")
    private String creditParty;

    @Column(name = "account_reference")
    private String accountReference;

    @Column(name = "category")
    private String category;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}