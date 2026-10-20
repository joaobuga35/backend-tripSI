package com.tripsi.api.user.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void should_create_new_user_as_active_with_role_user_when_created() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");

        assertThat(user.getName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@tripsi.com");
        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(user.getRoles()).containsExactly(Role.ROLE_USER);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    void should_return_unmodifiable_roles_set() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");

        assertThatThrownBy(() -> user.getRoles().add(Role.ROLE_ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_update_name_and_touch_updated_at_when_update_profile_called() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        Instant originalUpdatedAt = user.getUpdatedAt();

        user.updateProfile("Jane Smith");

        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    void should_mark_status_deleted_and_set_deleted_at_when_soft_delete_called() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");

        user.softDelete();

        assertThat(user.getStatus()).isEqualTo(Status.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
    }
}
