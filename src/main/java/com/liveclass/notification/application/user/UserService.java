package com.liveclass.notification.application.user;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveclass.notification.application.user.dto.LoginRequest;
import com.liveclass.notification.application.user.dto.LoginResponse;
import com.liveclass.notification.application.user.dto.SignupRequest;
import com.liveclass.notification.application.user.port.UserRepositoryPort;
import com.liveclass.notification.domain.user.User;
import com.liveclass.notification.domain.user.UserRole;
import com.liveclass.notification.global.exception.BusinessException;
import com.liveclass.notification.global.exception.ErrorCode;
import com.liveclass.notification.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepositoryPort userRepositoryPort;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public UUID signup(SignupRequest request) {

		// 이메일 중복 검증
		if (userRepositoryPort.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		User user = User.builder()
			.name(request.name())
			.email(request.email())
			.password(passwordEncoder.encode(request.password())) // 비밀번호 암호화
			.phoneNumber(request.phoneNumber())
			.role(UserRole.USER)
			.build();

		User savedUser = userRepositoryPort.save(user);
		return savedUser.getId();
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		// 이메일 존재 여부 확인
		User user = userRepositoryPort.findByEmail(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		// 비밀번호 일치 여부 검증
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		// JWT 토큰 생성 (도메인의 상태인 UserRole을 String으로 변환하여 주입)
		String jwt = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole().name());

		// 응답 객체 반환
		return LoginResponse.of(jwt);
	}
}
