package com.fintech.Exam.repository;


import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletStatementRepository extends JpaRepository<com.fintech.Exam.domain.WalletStatement, Long> {
}
