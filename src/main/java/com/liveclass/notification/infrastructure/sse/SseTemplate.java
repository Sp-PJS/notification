package com.liveclass.notification.infrastructure.sse;

import com.liveclass.notification.global.exception.ErrorCode;
import com.liveclass.notification.global.exception.NotificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseTemplate {

	private final SseRepository sseRepository;
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 기본 유지

	//새로운 SSE 클라이언트를 연결하고, 최초 더미 데이터를 발송
	public SseEmitter createConnection(Long userId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		// 타임아웃 및 완료/에러 시 콜백 등록 (메모리 누수 방지)
		emitter.onCompletion(() -> sseRepository.deleteByUserId(userId));
		emitter.onTimeout(() -> sseRepository.deleteByUserId(userId));
		emitter.onError((e) -> sseRepository.deleteByUserId(userId));

		sseRepository.save(userId, emitter);

		// 503 Service Unavailable 방지를 위한 최초 연결 더미 이벤트 발송
		try {
			emitter.send(SseEmitter.event()
				.id(userId.toString())
				.name("CONNECT")
				.data("SSE 세션 연결이 완료되었습니다."));
		} catch (IOException e) {
			sseRepository.deleteByUserId(userId);
			log.error("SSE 최초 연결 생성 중 에러 발생, userId={}", userId, e);
		}

		return emitter;
	}

	//저장된 세션을 찾아 실시간 알림 이벤트를 전송
	public void send(Long userId, String eventName, Object data) {
		sseRepository.findByUserId(userId).ifPresent(emitter -> {
			try {
				emitter.send(SseEmitter.event()
					.name(eventName)
					.data(data));
				log.info("SSE 알림 발송 성공: userId={}, event={}", userId, eventName);
			} catch (IOException e) {
				// 발송 실패 시 로컬 레포지토리에서 세션을 끊고 자원 해제
				sseRepository.deleteByUserId(userId);
				log.warn("SSE 연결이 유효하지 않아 세션을 제거합니다: userId={}", userId);
			}
		});
	}
}