package com.kb.healthcare.service;

import com.kb.healthcare.domain.User;
import com.kb.healthcare.dto.auth.LoginRequest;
import com.kb.healthcare.dto.auth.LoginResponse;
import com.kb.healthcare.dto.auth.SignupRequest;
import com.kb.healthcare.repository.UserRepository;
import com.kb.healthcare.security.JwtTokenProvider;
import com.kb.healthcare.support.CustomException;
import com.kb.healthcare.support.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 인증/인가 관련 비즈니스 로직을 처리하는 서비스
 *
 * <p>회원가입, 로그인 기능을 제공하며 JWT 토큰 기반 인증을 처리합니다.</p>
 *
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest req) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(req.email()))
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);

        // recordKey 중복 검증
        if (userRepository.existsByRecordKey(req.recordKey()))
            throw new CustomException(ErrorCode.DUPLICATE_RECORD_KEY);

        // 비밀번호 암호화 후 사용자 생성 및 저장
        User user = new User(req.recordKey(), req.name(), req.nickname(),
                req.email(), encoder.encode(req.password()));
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest req) {
        // Spring Security를 통한 인증 처리
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // 인증 성공 시 JWT 토큰 생성 및 반환
        String token = jwtTokenProvider.generate(req.email());
        return new LoginResponse(token);
    }
}