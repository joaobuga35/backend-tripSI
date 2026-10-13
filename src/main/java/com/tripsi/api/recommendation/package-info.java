/**
 * Recommendation module — analyses flight offers through LLM to rank and score options.
 *
 * <p>Exposes: {@code RecommendationServicePort}, {@code RecommendationController}</p>
 * <p>Listens: {@code FlightOffersReceivedEvent} from flight module</p>
 * <p>Publishes: {@code RecommendationReadyEvent} → consumed by planner module</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "flight"}
)
package com.tripsi.api.recommendation;
