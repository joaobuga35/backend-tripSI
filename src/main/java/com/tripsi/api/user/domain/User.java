package com.tripsi.api.user.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Framework-agnostic user aggregate; no dependency on Spring Security (see UserPrincipal adapter).
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Version
    private long version;

    protected User() {
        // required by JPA
    }

    private User(String name, String email, String hashedPassword) {
        this.name = name;
        this.email = email;
        this.password = hashedPassword;
        this.roles = new HashSet<>(Set.of(Role.ROLE_USER));
        this.status = Status.ACTIVE;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // hashedPassword must already be encoded by the caller (e.g. UserService via PasswordEncoder).
    public static User createNew(String name, String email, String hashedPassword) {
        return new User(name, email, hashedPassword);
    }

    public void updateProfile(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.status = Status.DELETED;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public long getVersion() {
        return version;
    }
}
