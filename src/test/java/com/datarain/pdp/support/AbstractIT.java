package com.datarain.pdp.support;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIT {

    @Autowired
    private Flyway flyway;

    @BeforeAll
    void resetDatabaseOnce() {
        flyway.clean();
        flyway.migrate();
        RestAssured.filters(new ColorRestAssuredFilter());
    }
}
