package com.fintech.Exam.repository;

import com.fintech.Exam.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<Transaction, Long> {

}
