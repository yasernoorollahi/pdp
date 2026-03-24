package com.datarain.pdp.it.rest.message;

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
class UserMessageFlowRestIT extends AbstractIT {

    @LocalServerPort
    int port;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void should_create_update_list_and_delete_message() {
        String accessToken = registerAndGetAccessToken();

        String createPayload = """
                {
                  "content": "Hello world",
                  "messageDate": "2024-01-01"
                }
                """;

        String messageId = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(createPayload)
                .when()
                .post("/api/user-messages")
                .then()
                .statusCode(restStatus(201))
                .body("id", notNullValue())
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/user-messages/{id}", messageId)
                .then()
                .statusCode(restStatus(200))
                .body("id", equalTo(messageId));

        String updatePayload = """
                {
                  "content": "Updated content",
                  "messageDate": "2024-01-02"
                }
                """;

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(updatePayload)
                .when()
                .put("/api/user-messages/{id}", messageId)
                .then()
                .statusCode(restStatus(200))
                .body("content", equalTo("Updated content"));

        String processedPayload = """
                {
                  "processed": true
                }
                """;

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(processedPayload)
                .when()
                .patch("/api/user-messages/{id}/processed", messageId)
                .then()
                .statusCode(restStatus(200))
                .body("processed", equalTo(true));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/user-messages")
                .then()
                .statusCode(restStatus(200))
                .body("content[0].id", equalTo(messageId));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/user-messages/{id}", messageId)
                .then()
                .statusCode(restStatus(204));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/user-messages/{id}", messageId)
                .then()
                .statusCode(restStatus(404));
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
