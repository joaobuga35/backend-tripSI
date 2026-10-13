package com.tripsi.api;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void should_verify_module_structure() {
        ApplicationModules modules = ApplicationModules.of(TripSiApplication.class);
        modules.verify();
    }

    @Test
    void should_document_modules() {
        ApplicationModules modules = ApplicationModules.of(TripSiApplication.class);
        // Prints the module structure to console — useful for development
        modules.forEach(System.out::println);
    }
}
