package com.tripsi.api.user.application;

import com.tripsi.api.shared.exception.BusinessRuleException;
import com.tripsi.api.shared.exception.ResourceNotFoundException;
import com.tripsi.api.user.domain.Role;
import com.tripsi.api.user.domain.Status;
import com.tripsi.api.user.domain.User;
import com.tripsi.api.user.dto.RegisterRequestDTO;
import com.tripsi.api.user.dto.UpdateUserDTO;
import com.tripsi.api.user.dto.UserMapperImpl;
import com.tripsi.api.user.dto.UserResponseDTO;
import com.tripsi.api.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, new UserMapperImpl());
    }

    @Test
    void should_register_user_with_hashed_password_when_email_not_taken() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@tripsi.com", "plain-password");
        when(userRepository.existsByEmail("jane@tripsi.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.register(request);

        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane@tripsi.com");
        assertThat(response.status()).isEqualTo(Status.ACTIVE);
        assertThat(response.roles()).containsExactly(Role.ROLE_USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
    }

    @Test
    void should_throw_business_rule_exception_when_email_already_registered() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@tripsi.com", "plain-password");
        when(userRepository.existsByEmail("jane@tripsi.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void should_find_user_by_id_when_exists() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findById(id);

        assertThat(response.email()).isEqualTo("jane@tripsi.com");
    }

    @Test
    void should_throw_resource_not_found_exception_when_id_does_not_exist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_find_user_by_email_when_exists() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        when(userRepository.findByEmail("jane@tripsi.com")).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findByEmail("jane@tripsi.com");

        assertThat(response.name()).isEqualTo("Jane Doe");
    }

    @Test
    void should_throw_resource_not_found_exception_when_email_does_not_exist() {
        when(userRepository.findByEmail("missing@tripsi.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@tripsi.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_update_name_when_user_exists() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.update(id, new UpdateUserDTO("Jane Smith"));

        assertThat(response.name()).isEqualTo("Jane Smith");
    }

    @Test
    void should_throw_resource_not_found_exception_when_updating_missing_user() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, new UpdateUserDTO("Jane Smith")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_soft_delete_user_when_exists() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.softDelete(id);

        assertThat(user.getStatus()).isEqualTo(Status.DELETED);
        verify(userRepository).save(user);
    }

    @Test
    void should_throw_resource_not_found_exception_when_soft_deleting_missing_user() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.softDelete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
