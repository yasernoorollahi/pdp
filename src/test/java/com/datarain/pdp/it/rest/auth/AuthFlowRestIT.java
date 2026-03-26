package com.datarain.pdp.it.rest.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import com.datarain.pdp.infrastructure.security.jwt.JwtService;
import com.datarain.pdp.support.AbstractIT;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

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

        Tokens registerTokens =
                given()
                        .contentType("application/json")
                        .body(registerPayload)
                        .when()
                        .post("/api/auth/register")
                        .then()
                        .statusCode(restStatus(201))
                        .body("accessToken", notNullValue())
                        .extract()
                        .as(Tokens.class);

        // 2. Call protected API
        given()
                .header("Authorization", "Bearer " + registerTokens.accessToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(restStatus(200))
                .body("email", equalTo(email));

        // 3. Login success
        Tokens loginTokens = login(email, password, 200);

        // 4. Get user id (for admin ops)
        String userId = given()
                .header("Authorization", "Bearer " + loginTokens.accessToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(restStatus(200))
                .extract()
                .path("id");

        // 5. Prepare one failed login to trigger lockout
        jdbcTemplate.update(
                "update users set failed_login_attempts = 4 where email = ?",
                email
        );

        // 6. Login failed → account locked
        login(email, "wrong-password", 401);

        // 7. Locked account should reject valid login
        login(email, password, 403);

        // 8. Admin unlock
        String adminAccessToken = adminAccessToken();
        given()
                .header("Authorization", "Bearer " + adminAccessToken)
                .when()
                .post("/api/users/" + userId + "/unlock")
                .then()
                .statusCode(restStatus(204));

        // 9. Admin disable account
        given()
                .header("Authorization", "Bearer " + adminAccessToken)
                .when()
                .patch("/api/users/" + userId + "/enabled?enabled=false")
                .then()
                .statusCode(restStatus(204));

        // 10. Admin enable account
        given()
                .header("Authorization", "Bearer " + adminAccessToken)
                .when()
                .patch("/api/users/" + userId + "/enabled?enabled=true")
                .then()
                .statusCode(restStatus(204));

        // 11. Refresh token
        Tokens refreshedTokens =
                given()
                        .contentType("application/json")
                        .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(loginTokens.refreshToken))
                        .when()
                        .post("/api/auth/refresh")
                        .then()
                        .statusCode(restStatus(200))
                        .body("accessToken", notNullValue())
                        .body("refreshToken", notNullValue())
                        .extract()
                        .as(Tokens.class);

        // 12. Logout (single device)
        given()
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(restStatus(204));

        // 13. Logout all devices
        given()
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when()
                .post("/api/auth/logout-all")
                .then()
                .statusCode(restStatus(204));
    }

    private Tokens login(String email, String password, int expectedStatus) {
        String payload = """
        {
          "email": "%s",
          "password": "%s"
        }
        """.formatted(email, password);

        if (expectedStatus == 200) {
            return given()
                    .contentType("application/json")
                    .body(payload)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(restStatus(expectedStatus))
                    .extract()
                    .as(Tokens.class);
        }

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(restStatus(expectedStatus));
        return null;
    }

    private String adminAccessToken() {
        User admin = userRepository.findByEmail("admin@pdp.local")
                .orElseThrow();
        return jwtService.generateAccessToken(admin);
    }

    private static final class Tokens {
        public String accessToken;
        public String refreshToken;

        private Tokens() {
        }
    }
}
