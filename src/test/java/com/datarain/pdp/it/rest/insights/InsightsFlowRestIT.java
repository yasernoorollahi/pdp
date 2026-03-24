package com.datarain.pdp.it.rest.insights;

import com.datarain.pdp.support.AbstractIT;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static com.datarain.pdp.support.TestExpectations.restStatus;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InsightsFlowRestIT extends AbstractIT {

    @LocalServerPort
    int port;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void should_fetch_insights_with_empty_data() {
        String accessToken = registerAndGetAccessToken();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/insights/today")
                .then()
                .statusCode(restStatus(200))
                .body("energy", equalTo(0.0f));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/insights/summary")
                .then()
                .statusCode(restStatus(200))
                .body("energy", notNullValue())
                .body("motivation", notNullValue());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/insights/timeline")
                .then()
                .statusCode(restStatus(200));
    }

    private String registerAndGetAccessToken() {
        String email = "user-" + UUID.randomUUID() + "@pdp.local";
        String password = "password123";

        String payload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        return given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(restStatus(201))
                .extract()
                .path("accessToken");
    }
}
