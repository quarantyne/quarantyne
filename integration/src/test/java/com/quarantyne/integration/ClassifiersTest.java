package com.quarantyne.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;

public class ClassifiersTest {

  OkHttpClient httpClient = new OkHttpClient();

  String USER_AGENT =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:63.0) Gecko/20100101 Firefox/63.0";

  @Before
  public void before() throws InterruptedException{
    Thread.sleep(900);
  }

  @Test
  public void testCompromisedPasswordJson() {
    given()
        .contentType("application/json")
        .body(new JsonObject().put("password", "123").toString())
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("CPW"));
  }

  @Test
  public void testCompromisedPasswordMultipart() {
    given()
        .formParams("password", "123")
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("CPW"));
  }


  @Test
  public void testDisposableEmailJson() {
    given()
        .contentType("application/json")
        .body(new JsonObject().put("email", "eddy@dispostable.com").toString())
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("DMX"));
  }

  @Test
  public void testDisposableEmailMultipart() {
    given()
        .contentType("application/x-www-form-urlencoded; charset=ISO-8859-1 ")
        .body("email=eddy@dispostable.com")
        .post("/anything")
        .then()
        .log().all()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("DMX"));
  }

  /*
  @Test
  public void testDisposableEmaiFormParam() {
    given()
        .contentType("application/x-www-form-urlencoded; charset=ISO-8859-1 ")
        .body("email=eddy@dispostable.com")
        .post("/anything")
        .then()
        .log().all()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("DMX"));
  }
  */
  @Test
  public void testLargeBodySize() {
    given()
        .contentType("application/json")
        .body(new JsonObject().put("k", new String(new byte[1_024])).toString())
        .post("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("LBD"));
  }

  @Test
  public void testSuspiciousUserAgent() {
    given()
        .contentType("application/json")
        .header("user-agent", "curl/7.54.0")
        .get("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("SUA"));
  }

  @Test
  public void testSuspiciousHeaders() {
    given()
        .headers(Maps.newHashMap())
        .get("/anything")
        .then()
        .statusCode(equalTo(200))
        .log().body()
        .body("headers.X-Quarantyne-Labels", equalTo("SHR"));
  }

  @Test
  public void testFastAgent() throws IOException {
    Request req = new Request.Builder().header("user-agent", USER_AGENT).url("http://localhost:8080").build();
    httpClient.newCall(req).execute();
    httpClient.newCall(req).execute();

    given()
        .header("DNT", 1)
        .header("user-agent", USER_AGENT)
        .get("/anything")
        .then()
        .statusCode(equalTo(200))
        .body("headers.X-Quarantyne-Labels", equalTo("FAS"));
  }
}
