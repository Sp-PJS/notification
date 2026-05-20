package com.liveclass.notification.presentation.notification;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.liveclass.notification.infrastructure.messaging.NotificationSenderAdapter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {

	private final NotificationSenderAdapter notificationSenderAdapter;

	/**
	 * 클라이언트 실시간 인앱 회선 연결 채널 오픈 API
	 */
	@GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(@PathVariable UUID userId) {
		// 내부 연결 메서드 호출
		SseEmitter emitter = notificationSenderAdapter.createConnection(userId);
		return ResponseEntity.ok(emitter);
	}
}