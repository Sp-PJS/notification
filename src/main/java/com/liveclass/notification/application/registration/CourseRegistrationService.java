package com.liveclass.notification.application.registration;

import com.liveclass.notification.application.registration.dto.CourseRegistrationRequest;
import com.liveclass.notification.application.registration.port.CourseRegistrationRepositoryPort;
import com.liveclass.notification.domain.course.Course;
import com.liveclass.notification.domain.registration.CourseRegistration;
import com.liveclass.notification.domain.registration.RegistrationStatus;
import com.liveclass.notification.domain.user.User;
import com.liveclass.notification.domain.user.UserRole;
import com.liveclass.notification.global.exception.ErrorCode;
import com.liveclass.notification.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseRegistrationService {

	private final CourseRegistrationRepositoryPort registrationRepositoryPort;

	@Transactional
	public UUID registerCourse(UUID authenticatedUserId, CourseRegistrationRequest request) {

		// 토큰에서 추출한 ID가 실제 DB에 존재하는 정상적인 유저인지 체크
		User user = registrationRepositoryPort.findUserById(authenticatedUserId)
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

		// 권한체크
		if (user.getRole() != UserRole.USER) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		// 강의 존재 여부 확인
		Course course = registrationRepositoryPort.findCourseById(request.courseId())
			.orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

		// 중복 신청 방지
		if (registrationRepositoryPort.existsByUserAndCourse(user, course)) {
			throw new BusinessException(ErrorCode.ALREADY_REGISTERED);
		}

		CourseRegistration registration = CourseRegistration.builder()
			.user(user)
			.course(course)
			.status(RegistrationStatus.COMPLETED)
			.build();

		// TODO: 알림 이벤트 발행 위치

		return registrationRepositoryPort.save(registration).getId();
	}

	@Transactional
	public UUID cancelCourseRegistration(UUID authenticatedUserId, UUID registrationId) {

		// 수강신청 내역 존재 여부 확인
		CourseRegistration registration = registrationRepositoryPort.findById(registrationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));

		// 본인 확인
		if (!registration.getUser().getId().equals(authenticatedUserId)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		// 3. 도메인 객체 상태 변경 (COMPLETED -> CANCELED)
		registration.cancel();

		// 4. 변경된 상태 DB에 반영 후, 저장된 엔티티의 ID를 반환
		CourseRegistration savedRegistration = registrationRepositoryPort.save(registration);

		// TODO: 알림 이벤트 발행 위치

		return savedRegistration.getId();
	}
}