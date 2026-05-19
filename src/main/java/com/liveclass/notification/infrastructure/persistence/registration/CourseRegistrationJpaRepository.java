package com.liveclass.notification.infrastructure.persistence.registration;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liveclass.notification.domain.course.Course;
import com.liveclass.notification.domain.registration.CourseRegistration;
import com.liveclass.notification.domain.user.User;

public interface CourseRegistrationJpaRepository extends JpaRepository<CourseRegistration, UUID> {
	boolean existsByUserAndCourse(User user, Course course);
}
