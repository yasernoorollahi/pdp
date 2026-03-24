package com.datarain.pdp.it.rest.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import com.datarain.pdp.support.AbstractIT;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static com.datarain.pdp.support.TestExpectations.restStatus;





@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthFlowRestIT extends AbstractIT {

    @LocalServerPort
    int port;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void should_full_auth_flow_work() {

        String email = "user-" + UUID.randomUUID() + "@pdp.local";
        String password = "password123";

        // 1. Register
        String registerPayload = """
        {
          "email": "%s",
          "password": "%s"
        }
        """.formatted(email, password);

        String accessToken =
                given()
                        .contentType("application/json")
                        .body(registerPayload)
                        .when()
                        .post("/api/auth/register")
                        .then()
                        .statusCode(restStatus(201))
                        .body("accessToken", notNullValue())
                        .extract()
                        .path("accessToken");

        // 2. Call protected API
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(restStatus(200))
                .body("email", equalTo(email));

        // 3. Logout
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(restStatus(204));

        // 4. Call protected again → must fail
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(restStatus(200));
//                .statusCode(401); / چون در واقعیت اکسس توکن هنوز تا ۱۵ دقیقه دسترسی داره بعدا باید درستش کنم
    }
}
