package com.liveclass.notification.infrastructure.persistence.course;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liveclass.notification.domain.course.Course;

public interface CourseJpaRepository extends JpaRepository<Course, UUID> {
}
