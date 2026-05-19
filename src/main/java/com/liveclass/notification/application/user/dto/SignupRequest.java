package com.liveclass.notification.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank(message = "이름은 필수 입력 항목입니다.")
	@Size(max = 36, message = "이름은 36자 이하로 입력해주세요.")
	String name,

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@Size(max = 36, message = "이메일은 36자 이하로 입력해주세요.")
	String email,

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	@Size(min = 8, max = 36, message = "비밀번호는 8자 이상 36자 이하로 입력해주세요.")
	String password,

	@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
	String phoneNumber
) {
}