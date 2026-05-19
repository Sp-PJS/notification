package com.liveclass.notification.domain.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.liveclass.notification.domain.common.BaseEntity;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

	@Id
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 36)
	private String tutor;

	@Builder
	public Course(UUID id, String title, String tutor) {
		this.id = id != null ? id : UUID.randomUUID();
		this.title = title;
		this.tutor = tutor;
	}
}