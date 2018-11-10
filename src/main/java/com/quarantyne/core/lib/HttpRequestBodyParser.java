package com.quarantyne.core.lib;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

// TODO maybe migrate to jackson from gson?
@Slf4j
public final class HttpRequestBodyParser {
  private static String AMP_SEP = "&";
  private static String EQ_SEP = "=";
  private static Gson gson = new Gson();

  public static HttpRequestBody parse(byte[] body, String contentType) {
    ContentType ct = ContentTypeParser.parse(contentType);
    if (ct == null) {
      return null;
    }
    Map<String, Object> parsed = null;

    if (isJson(ct)) {
      parsed = parseAsJson(new String(body, ct.getCharset() != null ? ct.getCharset() : Charsets.UTF_8));
    } else if (isUrlEncoded(ct)) {
      parsed = parseAsUrlEncoded(new String(body, ct.getCharset() != null ? ct.getCharset() : Charsets.ISO_8859_1));
    }

    if (parsed != null) {
      return new HttpRequestBody(body, contentType, parsed);
    }

    return null;
  }

  private static boolean isJson(ContentType contentType) {
    return contentType.getContentType().equals(HttpHeaderValue.CONTENT_TYPE_JSON);
  }

  private static boolean isUrlEncoded(ContentType contentType) {
    return contentType.getContentType().equals(HttpHeaderValue.CONTENT_TYPE_URLENCODED);
  }

  private static Map<String, Object> parseAsJson(String body) {
    try {
      return gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
    } catch (JsonSyntaxException dex) {
      log.error("parseAsJson; cannot deserialize from JSON", dex);
      return null;
    }
  }


  private static Map<String, Object> parseAsUrlEncoded(String body) {
    String[] tokens = body.split(AMP_SEP);
    if (tokens.length > 0) {
      Map<String, Object> map = Maps.newHashMap();
      for (String e: tokens) {
        String[] kv = e.split(EQ_SEP);
        if (kv.length == 2) {
          map.put(kv[0], kv[1]);
        } else {
          log.warn("parseAsUrlEncoded: unexpected key/value token: {}", body);
        }
      }
      return map;
    } else {
      log.warn("parseAsUrlEncoded: no multipart data found {}", body);
      return null;
    }
  }
}
