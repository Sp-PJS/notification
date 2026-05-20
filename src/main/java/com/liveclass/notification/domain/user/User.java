package com.liveclass.notification.domain.user;

import java.util.UUID;

import com.liveclass.notification.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(nullable = false, length = 36)
	private String name;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, unique = true, length = 36)
	private String email;

	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	@Builder // 생성자 레벨 빌더 패턴 적용
	public User(UUID id, String name, String email, String password, String phoneNumber, UserRole role) {
		this.id = id != null ? id : UUID.randomUUID(); // ID가 넘어오지 않으면 자바에서 자동 생성
		this.name = name;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.role = role != null ? role : UserRole.USER;
	}
}