package com.quarantyne.integration;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.head;
import static io.restassured.RestAssured.options;
import static io.restassured.RestAssured.patch;
import static io.restassured.RestAssured.post;
import static io.restassured.RestAssured.put;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class MethodsWithoutBodyTest extends AbstractTest {

  @Test
  public void testGet() {
    get("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.Host", equalTo("httpbin.org"));
  }
  @Test
  public void testPost() {
    post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.Host", equalTo("httpbin.org"));
  }
  @Test
  public void testPut() {
    put("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.Host", equalTo("httpbin.org"));
  }
  @Test
  public void testPatch() {
    patch("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.Host", equalTo("httpbin.org"));
  }
  @Test
  public void testDelete() {
    delete("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.Host", equalTo("httpbin.org"));
  }
  @Test
  public void testHead() {
    head("/anything")
        .then()
        .statusCode(equalTo(200))
        .body(equalTo(""));
  }
  @Test
  public void testOptions() {
    options("/anything")
        .then()
        .statusCode(equalTo(200))
        .body(equalTo(""));
  }
}
