package com.fintech.Exam.repository;

import com.fintech.Exam.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByDebitParty(String debitParty);
    @Query(value = "SELECT * FROM wallet w WHERE " +
            "(:debitParty IS NULL OR w.debit_party ILIKE CONCAT('%', :debitParty, '%')) AND " ,
            nativeQuery = true)
    List<Wallet> searchTransaction(
            @Param("debit_party") String debitParty
    );
}
