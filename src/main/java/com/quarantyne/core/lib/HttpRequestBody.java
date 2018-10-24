package com.quarantyne.core.lib;

import com.google.common.base.Strings;
import java.util.Map;
import lombok.Value;

/**
 * Assume UTF8
 */
@Value
public class HttpRequestBody {

  private final byte[] body;
  private final String contentType;
  private final Map<String, Object> parsedBody;

  public HttpRequestBody(byte[] body, String contentType, Map<String, Object> parsedBody) {
    this.body = body;
    this.contentType = contentType;
    this.parsedBody = parsedBody;
  }

  public String get(String key) {
    if (Strings.isNullOrEmpty(key)|| parsedBody == null) {
      return null;
    }
    Object value = parsedBody.get(key);
    if (value instanceof String) {
      return (String)value;
    } else {
      return null;
    }
  }
}
