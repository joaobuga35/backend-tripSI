package com.tripsi.api.user.adapter.in.security;

import com.tripsi.api.user.domain.User;
import com.tripsi.api.user.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void should_return_user_principal_when_email_exists() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        when(userRepository.findByEmail("jane@tripsi.com")).thenReturn(Optional.of(user));
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        UserDetails result = service.loadUserByUsername("jane@tripsi.com");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("jane@tripsi.com");
    }

    @Test
    void should_throw_username_not_found_when_email_missing() {
        when(userRepository.findByEmail("missing@tripsi.com")).thenReturn(Optional.empty());
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing@tripsi.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
