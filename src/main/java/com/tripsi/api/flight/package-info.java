/**
 * Flight module — searches for real flight offers via Duffel API (or mock adapter).
 *
 * <p>Exposes: {@code FlightServicePort}, {@code FlightController}</p>
 * <p>Listens: {@code SuggestionCompletedEvent} from suggestion module</p>
 * <p>Publishes: {@code FlightOffersReceivedEvent} → consumed by recommendation module</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "suggestion"}
)
package com.tripsi.api.flight;
