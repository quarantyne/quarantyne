package com.quarantyne.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.patch;
import static io.restassured.RestAssured.post;
import static io.restassured.RestAssured.put;
import static org.hamcrest.Matchers.equalTo;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class MethodsWithBodyTest {
  @Test
  public void testPost_plainText() {
    given().body("hello world")
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("data", equalTo("hello world"));
  }

  @Test
  public void testPost_json_noContentType() {
    String json = new JsonObject().put("hello", "world").toString();
    given().body(json)
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("data", equalTo(json));
  }
}
