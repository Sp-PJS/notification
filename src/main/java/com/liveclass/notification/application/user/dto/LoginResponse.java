package com.liveclass.notification.application.user.dto;

public record LoginResponse(
	String jwt,
	String tokenType
) {
	public static LoginResponse of(String jwt) {
		return new LoginResponse(jwt, "Bearer");
	}
}