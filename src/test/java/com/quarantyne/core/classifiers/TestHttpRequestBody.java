package com.quarantyne.core.classifiers;

import com.google.common.collect.Maps;
import com.quarantyne.core.lib.HttpHeaderValue;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.lib.HttpRequestBodyParser;
import io.vertx.core.json.JsonObject;

public class TestHttpRequestBody {
  public static HttpRequestBody EMPTY = new HttpRequestBody(
      new byte[]{}, "", Maps.newHashMap());

  public static HttpRequestBody make(JsonObject o) {
    byte[] bytes = o.toBuffer().getBytes();
    return HttpRequestBodyParser.parse(bytes, HttpHeaderValue.CONTENT_TYPE_JSON);
  }
}
