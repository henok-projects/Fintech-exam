package com.fintech.Exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_wallet")
@Getter
@Setter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "debit_party")
    private String debitParty;
    private BigDecimal balance;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public static Wallet create(Application application, LocalDateTime createdAt) {
        Wallet wallet = new Wallet();
        wallet.setDebitParty(application.getDebitParty());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(createdAt);
        wallet.setUpdatedAt(createdAt);
        return wallet;
    }

}
