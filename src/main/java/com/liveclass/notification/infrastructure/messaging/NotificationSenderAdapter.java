package com.liveclass.notification.infrastructure.messaging;

import com.liveclass.notification.application.notification.dto.NotificationCreateRequest;
import com.liveclass.notification.application.notification.port.NotificationSenderPort;
import com.liveclass.notification.infrastructure.sse.SseConnectionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSenderAdapter implements NotificationSenderPort {

	private final SseConnectionRegistry sseConnectionRegistry;
	private static final Long CONNECTION_TIMEOUT = 30L * 60 * 1000; // 30분 기한

	// SseController에서 실시간 연결을 맺기 위해 사용하는 메서드
	public SseEmitter createConnection(UUID userId) {
		SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT);

		emitter.onCompletion(() -> sseConnectionRegistry.delete(userId));
		emitter.onTimeout(() -> sseConnectionRegistry.delete(userId));
		emitter.onError((e) -> sseConnectionRegistry.delete(userId));

		sseConnectionRegistry.save(userId, emitter);

		// 503 방지 브라우저 스트리밍 더미 핑 발송
		try {
			emitter.send(SseEmitter.event().name("INIT").data("Connected successfully."));
		} catch (IOException e) {
			sseConnectionRegistry.delete(userId);
			log.error("SSE 초기 연결 바인딩 오류 발생 - 유저 ID: {}", userId);
		}

		return emitter;
	}

	//Application 레이어(NotificationService)에서 포트를 통해 호출하는 인앱 발송 구현체
	@Override
	public void sendInApp(NotificationCreateRequest request) {

		String cleanContent = sanitizeContent(request.content());

		// 실제 이메일 발송 없이 로그 출력(Mock) 대체
		log.info("[IN_APP Mock 전송] To: {}, 본문: \"{}\"", request.userId(), cleanContent);

		sseConnectionRegistry.get(request.userId()).ifPresent(emitter -> {
			try {
				// 알림 스펙에 맞게 event 네이밍과 데이터를 밀어넣기
				emitter.send(SseEmitter.event().name("NOTIFICATION_RECEIVED").data(request.content()));
				log.info("[SSE 발송 완료] 유저 ID: {}, 내용: {}", request.userId(), request.content());
			} catch (IOException e) {
				sseConnectionRegistry.delete(request.userId());
				log.warn("유효하지 않은 SSE 스트림 제거 세션 분리 - 유저 ID: {}", request.userId());
				throw new RuntimeException("SSE 전송 중 회선 장애 발생", e); // 예외를 던져 서비스의 catch 블록이 동작하도록 유도
			}
		});
	}

	//Application 레이어(NotificationService)에서 포트를 통해 호출하는 이메일 발송 구현체
	@Override
	public void sendEmail(NotificationCreateRequest request) {

		String cleanContent = sanitizeContent(request.content());

		// 실제 이메일 발송 없이 로그 출력(Mock) 대체
		log.info("[EMAIL Mock 전송] To: {}, 본문: \"{}\"", request.userId(), cleanContent);
	}

	// 로깅 문구 정제
	private String sanitizeContent(String content) {
		if (content != null && content.startsWith("\"")) {
			return content.substring(1);
		}
		return content;
	}
}