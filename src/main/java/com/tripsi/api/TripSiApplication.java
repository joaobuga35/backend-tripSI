package com.tripsi.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(
        systemName = "TripSi",
        sharedModules = "shared"
)
public class TripSiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripSiApplication.class, args);
    }
}
