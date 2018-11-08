package com.quarantyne.core.lib;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
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
    this.body = Preconditions.checkNotNull(body);
    this.contentType = Preconditions.checkNotNull(contentType);
    this.parsedBody = Preconditions.checkNotNull(parsedBody);
  }

  public String get(String key) {
    if (Strings.isNullOrEmpty(key)) {
      return null;
    }
    Object value = parsedBody.get(key);
    if (value instanceof String) {
      return (String)value;
    } else {
      return null;
    }
  }

  @Nullable
  public String getAny(Set<String> keys) {
    if (keys == null) {
      return null;
    }
    String value = null;
    for (String key: keys) {
      value = get(key);
      if (value != null) {
        break;
      }
    }
    return value;
  }

  public boolean containsAny(Set<String> keys) {
    return keys != null
        && keys.stream().anyMatch(parsedBody::containsKey);
  }
}
