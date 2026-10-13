/**
 * Planner module — generates day-by-day trip itineraries based on recommendations.
 *
 * <p>Exposes: {@code PlannerServicePort}, {@code PlannerController}</p>
 * <p>Listens: {@code RecommendationReadyEvent} from recommendation module</p>
 * <p>Publishes: {@code PlannerCompletedEvent} (terminal event — full pipeline complete)</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "recommendation"}
)
package com.tripsi.api.planner;
