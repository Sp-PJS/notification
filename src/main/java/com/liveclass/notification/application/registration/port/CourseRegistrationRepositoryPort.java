package com.liveclass.notification.application.registration.port;

import com.liveclass.notification.domain.course.Course;
import com.liveclass.notification.domain.registration.CourseRegistration;
import com.liveclass.notification.domain.user.User;
import java.util.Optional;
import java.util.UUID;

public interface CourseRegistrationRepositoryPort {
	Optional<User> findUserById(UUID userId);
	Optional<Course> findCourseById(UUID courseId);
	CourseRegistration save(CourseRegistration registration);
	boolean existsByUserAndCourse(User user, Course course);
	Optional<CourseRegistration> findById(UUID id);
}