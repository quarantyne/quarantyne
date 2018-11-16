package com.quarantyne.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class HttpRequestBodyParserTest {
  static String JSON = HttpHeaderValue.CONTENT_TYPE_JSON;
  static String MULTIPART = HttpHeaderValue.CONTENT_TYPE_URLENCODED;

  @Test
  public void testParseJson() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        new JsonObject().put("key", "value").toString().getBytes(Charsets.UTF_8),
        JSON);
    assertThat(httpRequestBody.get("key")).isEqualTo("value");
    assertThat(httpRequestBody.get("boo")).isNull();
  }

  @Test
  public void testParseBadJson() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "boo it's not json".getBytes(Charsets.UTF_8),
        JSON);
    assertThat(httpRequestBody).isNull();
  }

  @Test
  public void testParseMultipart() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "key=value&param=true".getBytes(Charsets.ISO_8859_1),
        MULTIPART);
    assertThat(httpRequestBody.get("key")).isEqualTo("value");
    assertThat(httpRequestBody.get("boo")).isNull();
  }

  @Test
  public void testParseContentTypeWithChartset() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "key=value&param=true".getBytes(Charsets.ISO_8859_1),
        MULTIPART);
    assertThat(httpRequestBody.get("key")).isEqualTo("value");
    assertThat(httpRequestBody.get("boo")).isNull();
  }

  @Test
  public void testParseBadMultipart() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "boo it's not multipart".getBytes(Charsets.ISO_8859_1),
        MULTIPART);
    assertThat(httpRequestBody.get("key")).isNull();
  }
}
