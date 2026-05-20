package com.liveclass.notification.domain.notification;

import com.liveclass.notification.domain.common.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 중복 알림 방지를 위한 제약조건
@Table(name = "notifications",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_notification_event_key",
			columnNames = {"event_key"}
		)
	}
)
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 36)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 36)
	private NotificationStatus status;

	@Column(nullable = false, length = 100)
	private String eventKey; // 중복 알림 방지

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(nullable = false)
	private int retryCount = 0;

	@Column(columnDefinition = "TEXT")
	private String lastErrorMessage;

	@Builder
	public Notification(UUID userId, NotificationChannel channel, String eventKey, String content) {
		this.userId = userId;
		this.channel = channel;
		this.eventKey = eventKey;
		this.content = content;
		this.status = NotificationStatus.PENDING;
	}

	// 상태 변경(처리 중)
	public void processing() {
		this.status = NotificationStatus.PROCESSING;
	}

	// 상태 변경(발송 성공)
	public void success() {
		this.status = NotificationStatus.SUCCESS;
		this.lastErrorMessage = null; // 성공 시 에러 메시지 초기화
	}

	// 상태 변경(발송 실패)
	public void fail(String errorMessage) {
		this.status = NotificationStatus.FAILED;
		this.lastErrorMessage = errorMessage;
		this.retryCount += 1;
	}

	// 상태 변경(최종 실패)
	public void permanentFail(String errorMessage) {
		this.status = NotificationStatus.FAILED_PERMANENT;
		this.lastErrorMessage = errorMessage;
	}
}