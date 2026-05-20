package com.liveclass.notification.application.registration;

import java.util.UUID;

import com.liveclass.notification.domain.registration.RegistrationStatus;

public record CourseRegistrationEvent(
	UUID userId,
	UUID courseId,
	RegistrationStatus status
) {
}