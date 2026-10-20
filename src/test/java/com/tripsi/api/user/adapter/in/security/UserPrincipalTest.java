package com.tripsi.api.user.adapter.in.security;

import com.tripsi.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void should_expose_email_as_username_and_hashed_password() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getUsername()).isEqualTo("jane@tripsi.com");
        assertThat(principal.getPassword()).isEqualTo("hashed-password");
        assertThat(principal.getId()).isEqualTo(user.getId());
    }

    @Test
    void should_map_roles_to_granted_authorities() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void should_be_enabled_when_status_is_active() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
    }

    @Test
    void should_not_be_enabled_when_user_is_soft_deleted() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        user.softDelete();
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isEnabled()).isFalse();
    }
}
