/**
 * Suggestion module — processes user travel prompts through LLM to generate destination suggestions.
 *
 * <p>Exposes: {@code SuggestionServicePort}, {@code SuggestionController}</p>
 * <p>Publishes: {@code SuggestionCompletedEvent} → consumed by flight module</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "shared"
)
package com.tripsi.api.suggestion;
