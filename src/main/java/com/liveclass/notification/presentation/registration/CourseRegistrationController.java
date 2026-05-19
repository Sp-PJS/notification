package com.liveclass.notification.presentation.registration;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveclass.notification.application.registration.CourseRegistrationService;
import com.liveclass.notification.application.registration.dto.CourseRegistrationRequest;
import com.liveclass.notification.global.response.ApiResponse;
import com.liveclass.notification.infrastructure.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseRegistrationController {

	private final CourseRegistrationService registrationService;

	// 수강신청
	@PostMapping("/course-registrations")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<UUID>> registerCourse(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody CourseRegistrationRequest request
	) {
		UUID registrationId = registrationService.registerCourse(userDetails.getId(), request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.of(true, "수강신청이 정상적으로 완료되었습니다.", registrationId));
	}

	//수강신청 취소
	@PatchMapping("/course-registrations/{registrationId}/cancel")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<UUID>> cancelCourseRegistration(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("registrationId") UUID registrationId
	) {
		UUID cancelId = registrationService.cancelCourseRegistration(userDetails.getId(), registrationId);

		return ResponseEntity
			.ok()
			.body(ApiResponse.of(true, "수강신청 취소가 정상적으로 처리되었습니다.", cancelId));
	}
}
