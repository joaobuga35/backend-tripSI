/**
 * User module — registration, authentication, and user profile management.
 *
 * <p>Exposes: {@code UserServicePort}, {@code AuthController}, {@code UserController}</p>
 * <p>Publishes: no domain events (other modules don't depend on user lifecycle)</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "shared"
)
package com.tripsi.api.user;
