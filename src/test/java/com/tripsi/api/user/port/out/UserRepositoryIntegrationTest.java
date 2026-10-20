package com.tripsi.api.user.port.out;

import com.tripsi.api.user.domain.Role;
import com.tripsi.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_save_and_find_user_by_id() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jane@tripsi.com");
    }

    @Test
    void should_find_user_by_email_when_exists() {
        userRepository.save(User.createNew("Jane Doe", "jane2@tripsi.com", "hashed-password"));

        Optional<User> found = userRepository.findByEmail("jane2@tripsi.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Doe");
    }

    @Test
    void should_return_empty_when_email_does_not_exist() {
        Optional<User> found = userRepository.findByEmail("missing@tripsi.com");

        assertThat(found).isEmpty();
    }

    @Test
    void should_return_true_when_email_already_exists() {
        userRepository.save(User.createNew("Jane Doe", "jane3@tripsi.com", "hashed-password"));

        assertThat(userRepository.existsByEmail("jane3@tripsi.com")).isTrue();
    }

    @Test
    void should_return_false_when_email_does_not_exist_for_exists_check() {
        assertThat(userRepository.existsByEmail("missing@tripsi.com")).isFalse();
    }

    @Test
    void should_persist_roles_in_join_table() {
        User saved = userRepository.save(User.createNew("Jane Doe", "jane4@tripsi.com", "hashed-password"));
        userRepository.flush();

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).containsExactly(Role.ROLE_USER);
    }
}
