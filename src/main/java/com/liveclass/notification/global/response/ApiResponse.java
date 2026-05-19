package com.liveclass.notification.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final boolean success;
	private final String msg;
	private final T data;

	private ApiResponse(boolean success, String msg, T data) {
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

	// 성공/실패 메시지만 반환할 때
	public static <T> ApiResponse<T> of(boolean success, String msg) {
		return new ApiResponse<>(success, msg, null);
	}

	// 데이터를 함께 반환할 때
	public static <T> ApiResponse<T> of(boolean success, String msg, T data) {
		return new ApiResponse<>(success, msg, data);
	}
}
