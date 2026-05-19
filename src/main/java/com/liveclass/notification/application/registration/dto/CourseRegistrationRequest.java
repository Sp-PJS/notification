package com.liveclass.notification.application.registration.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CourseRegistrationRequest(
	@NotNull(message = "수강신청할 강의 ID는 필수입니다.")
	UUID courseId
) {
}