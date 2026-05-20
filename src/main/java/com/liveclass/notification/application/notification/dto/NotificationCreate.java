package com.liveclass.notification.application.notification.dto;

import java.util.UUID;

import com.liveclass.notification.domain.notification.NotificationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreate(
	@NotNull(message = "수신자 ID는 필수입니다.")
	UUID userId,                  // 수신자 ID

	@NotBlank(message = "알림 타입은 필수입니다.")
	String notificationType,      // 알림 타입

	@NotBlank(message = "참조 데이터는 필수입니다.")
	String courseId,         // 강의 ID

	@NotNull(message = "발송 채널은 필수입니다.")
	NotificationChannel channel   // 발송 채널

) {
}
