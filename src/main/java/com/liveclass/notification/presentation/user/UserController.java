package com.liveclass.notification.presentation.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveclass.notification.application.user.UserService;
import com.liveclass.notification.application.user.dto.LoginRequest;
import com.liveclass.notification.application.user.dto.LoginResponse;
import com.liveclass.notification.application.user.dto.SignupRequest;
import com.liveclass.notification.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<UUID>> signup(@Valid @RequestBody SignupRequest request) {
		UUID userId = userService.signup(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.of(true, "회원가입이 성공적으로 완료되었습니다.", userId));
	}

	// 로그인
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = userService.login(request);
		return ResponseEntity
			.ok()
			.body(ApiResponse.of(true, "로그인이 성공적으로 완료되었습니다.", response));
	}
}
