package com.fintech.Exam.service;

import com.fintech.Exam.domain.WalletUser;
import com.fintech.Exam.dto.AuthResponse;
import com.fintech.Exam.dto.LoginRequest;
import com.fintech.Exam.dto.RegisterRequest;
import com.fintech.Exam.exception.BadRequestException;
import com.fintech.Exam.exception.InvalidCredentialsException;
import com.fintech.Exam.repository.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final WalletUserRepository walletUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (walletUserRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already registered");
        }
        WalletUser user = newUser(username, passwordEncoder.encode(request.password()));
        return jwtService.createToken(walletUserRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();
        WalletUser user = walletUserRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return jwtService.createToken(user);
    }

    private WalletUser newUser(String username, String passwordHash) {
        WalletUser user = new WalletUser();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
