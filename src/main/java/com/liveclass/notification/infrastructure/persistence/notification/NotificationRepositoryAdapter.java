package com.liveclass.notification.infrastructure.persistence.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.liveclass.notification.application.notification.port.NotificationRepositoryPort;
import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

	private final NotificationJpaRepository notificationJpaRepository;

	@Override
	public Notification save(Notification notification) {
		return notificationJpaRepository.save(notification);
	}

	@Override
	public Optional<Notification> findById(UUID id) {
		return notificationJpaRepository.findById(id);
	}

	@Override
	public List<Notification> findAllByStatusAndRetryCountLessThan(NotificationStatus notificationStatus,
		int maxRetryLimit) {
		return notificationJpaRepository.findAllByStatusAndRetryCountLessThan(notificationStatus, maxRetryLimit);
	}

	@Override
	public List<Notification> findAllByUserId(UUID userId) {
		return notificationJpaRepository.findAllByUserId(userId);
	}
}
