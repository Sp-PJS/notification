package com.liveclass.notification.presentation.notification;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.liveclass.notification.application.notification.NotificationService;
import com.liveclass.notification.application.notification.dto.NotificationCreate;
import com.liveclass.notification.application.notification.dto.NotificationResponse;
import com.liveclass.notification.application.notification.dto.NotificationStatusResponse;
import com.liveclass.notification.global.response.ApiResponse;
import com.liveclass.notification.infrastructure.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	// 알림 발송 요청 등록
	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<UUID>> requestNotification(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody NotificationCreate request) {

		UUID notificationId = notificationService.registerNotificationRequest(userDetails.getId(), request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.of(true, "알림 발송 요청 등록이 완료되었습니다.", notificationId));
	}

	// 특정 알림 요청의 현재 상태 조회
	@GetMapping("/{notificationId}/status")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<NotificationStatusResponse>> getNotificationStatus(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID notificationId) {

		NotificationStatusResponse response = notificationService.getNotificationStatus(userDetails.getId(),
			notificationId);
		return ResponseEntity.ok(ApiResponse.of(true, "알림 상태 조회가 완료되었습니다.", response));
	}

	/* 사용자 알림 목록 조회
	/ isRead 읽음/안읽음 여부 필터 파라미터 */
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID userId,
		@RequestParam(value = "isRead", required = false) Boolean isRead) {

		List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userDetails.getId(), userId,
			isRead);
		return ResponseEntity.ok(ApiResponse.of(true, "사용자 알림 목록 조회가 완료되었습니다.", responses));
	}
}