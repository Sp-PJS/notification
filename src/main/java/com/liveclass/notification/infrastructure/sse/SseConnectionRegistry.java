package com.liveclass.notification.infrastructure.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseConnectionRegistry {

	private final Map<UUID, SseEmitter> registry = new ConcurrentHashMap<>();

	public void save(UUID userId, SseEmitter emitter) {
		this.registry.put(userId, emitter);
	}

	public Optional<SseEmitter> get(UUID userId) {
		return Optional.ofNullable(this.registry.get(userId));
	}

	public void delete(UUID userId) {
		this.registry.remove(userId);
	}
}