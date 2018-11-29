package com.quarantyne.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EndpointsTest extends AbstractTest {

  @Test
  @Ignore
  public void testHealthEndpoint() {
    given().port(3231).get("/health").then()
        .statusCode(equalTo(200))
        .body(equalTo("ok"));
  }

}
