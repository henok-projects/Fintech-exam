package com.fintech.Exam.repository;

import com.fintech.Exam.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByDebitParty(String debitParty);
}
