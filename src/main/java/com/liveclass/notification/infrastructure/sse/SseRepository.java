package com.liveclass.notification.infrastructure.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseRepository {

	// 유저 ID별로 SseEmitter 세션을 동시성 보장 맵으로 관리
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	//사용자의 SSE 연결 객체를 저장
	public SseEmitter save(Long userId, SseEmitter emitter) {
		this.emitters.put(userId, emitter);
		return emitter;
	}

	//특정 사용자의 SSE 연결 객체를 조회
	public Optional<SseEmitter> findByUserId(Long userId) {
		return Optional.ofNullable(this.emitters.get(userId));
	}

	//사용자가 로그아웃하거나 만료되었을 때 연결을 제거
	public void deleteByUserId(Long userId) {
		this.emitters.remove(userId);
	}
}