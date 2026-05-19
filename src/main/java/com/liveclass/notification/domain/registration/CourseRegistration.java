package com.liveclass.notification.domain.registration;

import java.util.UUID;

import com.liveclass.notification.domain.common.BaseEntity;
import com.liveclass.notification.domain.course.Course;
import com.liveclass.notification.domain.user.User;
import com.liveclass.notification.global.exception.BusinessException;
import com.liveclass.notification.global.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 중복 수강신청 방지를 위한 제약 조건
@Table(name = "course_registrations",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_user_course",
			columnNames = {"user_id", "course_id"}
		)
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRegistration extends BaseEntity {

	@Id
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 36)
	private RegistrationStatus status;

	// 생성자 레벨 빌더 패턴 적용
	@Builder
	public CourseRegistration(UUID id, User user, Course course, RegistrationStatus status) {
		this.id = id != null ? id : UUID.randomUUID();
		this.user = user;
		this.course = course;
		this.status = status != null ? status : RegistrationStatus.COMPLETED;
	}

	// 수강신청 취소
	public void cancel() {
		if (this.status == RegistrationStatus.CANCELED) {
			throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
		}
		this.status = RegistrationStatus.CANCELED;
	}
}
