package com.fintech.Exam.repository;

import com.fintech.Exam.domain.WalletUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletUserRepository extends JpaRepository<WalletUser, Long> {
    Optional<WalletUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
