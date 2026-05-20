package com.liveclass.notification.application.user.port;

import com.liveclass.notification.domain.user.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
	User save(User user);
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}