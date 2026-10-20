package com.tripsi.api.user.domain;

// Pure domain roles; mapping to Spring Security GrantedAuthority happens in the adapter layer.
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
