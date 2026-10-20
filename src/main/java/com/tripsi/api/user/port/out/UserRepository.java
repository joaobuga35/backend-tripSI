
package com.tripsi.api.user.port.out;

import com.tripsi.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Secondary port — Spring Data generates the adapter implementation at runtime.
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
