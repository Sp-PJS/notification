package com.liveclass.notification.global.exception;

import com.liveclass.notification.global.response.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		log.error("BusinessException 발생: {}", e.getErrorCode().getMessage());

		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(ApiResponse.of(false, e.getErrorCode().getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("예측하지 못한 서버 오류 발생: ", e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.of(false, "서버 내부 오류가 발생했습니다: ", null));
	}

	// 제약조건 위반 발생 시 처리 핸들러(중복 알림 방지)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.error("알림 데이터 제약조건 위반(알림 중복 요청 가능성 있음): ", e);

		ErrorCode duplicateError = ErrorCode.ALREADY_NOTIFIED;
		return ResponseEntity
			.status(duplicateError.getStatus())
			.body(ApiResponse.of(false, duplicateError.getMessage()));
	}
}