package bg.tu.varna.sit.si.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class LoggingControllerTest {

    @Test
    public void testHealthCheckEndpoint() {
        given()
          .when().get("/api/v1/ingest/health")
          .then()
             .statusCode(200)
             .body(is("{\"status\": \"UP\"}"));
    }

    @Test
    public void testIngestEndpoint() {
        String logMessage = "[{\"level\": \"INFO\", \"message\": \"This is a test log\", \"service\": \"test-service\"}]";

        given()
          .contentType(ContentType.JSON)
          .body(logMessage)
          .when().post("/api/v1/logs")
          .then()
             .statusCode(200);
    }
}