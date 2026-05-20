package com.liveclass.notification.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	private final long validityInMilliseconds = 3600000;

	private SecretKey key;
	private JwtParser jwtParser;

	@PostConstruct
	protected void init() {
		byte[] keyBytes = Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.jwtParser = Jwts.parser().verifyWith(this.key).build();
	}

	public String createToken(UUID userId, String email, String role) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInMilliseconds);

		return Jwts.builder()
			.subject(email)
			.claim("id", userId.toString()) // UUID 주입
			.claim("role", role)
			.issuedAt(now)
			.expiration(validity)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}

	public UUID getUserId(String token) {
		String idStr = jwtParser.parseSignedClaims(token).getPayload().get("id", String.class);
		return UUID.fromString(idStr);
	}

	public String getEmail(String token) {
		return jwtParser.parseSignedClaims(token).getPayload().getSubject();
	}

	public String getRole(String token) {
		return jwtParser.parseSignedClaims(token).getPayload().get("role", String.class);
	}

	public boolean validateToken(String token) {
		try {
			Jws<Claims> claims = jwtParser.parseSignedClaims(token);
			return !claims.getPayload().getExpiration().before(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}