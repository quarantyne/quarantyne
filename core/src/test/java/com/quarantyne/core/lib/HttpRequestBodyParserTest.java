package com.quarantyne.core.lib;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class HttpRequestBodyParserTest {
  static String JSON = "application/json";
  static String MULTIPART = "form-data/x-url-encoded";

  @Test
  public void testParseJson() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        new JsonObject().put("key", "value").toString().getBytes(Charsets.ISO_8859_1),
        JSON);
    assertThat(httpRequestBody.get("key")).isEqualTo("value");
    assertThat(httpRequestBody.get("boo")).isNull();
  }

  @Test
  public void testParseBadJson() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "boo it's not json".getBytes(Charsets.ISO_8859_1),
        JSON);
    assertThat(httpRequestBody.get("key")).isNull();
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
  public void testParseBadMultipart() {
    HttpRequestBody httpRequestBody = HttpRequestBodyParser.parse(
        "boo it's not multipart".getBytes(Charsets.ISO_8859_1),
        MULTIPART);
    assertThat(httpRequestBody.get("key")).isNull();
  }

  @Test
  public void testGetCharset() {
    assertThat(HttpRequestBodyParser.getCharset("some/content_type")).isEqualTo(Charsets.ISO_8859_1);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type;key")).isEqualTo(Charsets.ISO_8859_1);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type; key=value")).isEqualTo(Charsets.ISO_8859_1);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type;charset=utf-8")).isEqualTo(Charsets.UTF_8);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type; charset=utf-8")).isEqualTo(Charsets.UTF_8);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type; CHARSET=UTF-8")).isEqualTo(Charsets.UTF_8);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type; charset = utf-8")).isEqualTo(Charsets.UTF_8);
    assertThat(HttpRequestBodyParser.getCharset("some/content_type; key=value; charset=utf-8")).isEqualTo(Charsets.UTF_8);
  }
}
