package com.liveclass.notification.infrastructure.persistence.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationStatus;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findAllByStatusAndRetryCountLessThan(NotificationStatus notificationStatus, int maxRetryLimit);

	List<Notification> findAllByUserId(UUID userId);
}
