package com.liveclass.notification.infrastructure.persistence.registration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.liveclass.notification.application.registration.port.CourseRegistrationRepositoryPort;
import com.liveclass.notification.domain.course.Course;
import com.liveclass.notification.domain.registration.CourseRegistration;
import com.liveclass.notification.domain.user.User;
import com.liveclass.notification.infrastructure.persistence.course.CourseJpaRepository;
import com.liveclass.notification.infrastructure.persistence.user.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CourseRegistrationRepositoryAdapter implements CourseRegistrationRepositoryPort {

	private final CourseRegistrationJpaRepository courseRegistrationJpaRepository;
	private final UserJpaRepository userJpaRepository;
	private final CourseJpaRepository courseJpaRepository;

	@Override
	public Optional<User> findUserById(UUID userId) {
		return userJpaRepository.findById(userId);
	}

	@Override
	public Optional<Course> findCourseById(UUID courseId) {
		return courseJpaRepository.findById(courseId);
	}

	@Override
	public CourseRegistration save(CourseRegistration registration) {
		return courseRegistrationJpaRepository.save(registration);
	}

	@Override
	public boolean existsByUserAndCourse(User user, Course course) {
		return courseRegistrationJpaRepository.existsByUserAndCourse(user, course);
	}

	@Override
	public Optional<CourseRegistration> findById(UUID id) {
		return courseRegistrationJpaRepository.findById(id);
	}
}