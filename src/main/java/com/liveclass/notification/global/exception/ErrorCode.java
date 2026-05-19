package com.liveclass.notification.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	// 사용자 에러코드
	DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),

	// 수강신청 에러코드
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 자격 증명이 유효하지 않습니다."),
	COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."),
	ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 수강신청(또는 완료)된 강의입니다."),
	REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수강신청 내역입니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
