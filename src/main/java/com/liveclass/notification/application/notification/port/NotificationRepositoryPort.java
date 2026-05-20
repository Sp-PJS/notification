package com.liveclass.notification.application.notification.port;

import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {
	Notification save(Notification notification);
	Optional<Notification> findById(UUID id);
	List<Notification> findAllByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetryCount);
	List<Notification> findAllByUserId(UUID userId);

}