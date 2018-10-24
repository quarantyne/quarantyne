package com.quarantyne.core.lib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.Charset;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

// TODO maybe migrate to jackson from gson?
@Slf4j
public final class HttpRequestBodyParser {
  private static String AMP_SEP = "&";
  private static String EQ_SEP = "=";
  private static String SEMICOL = ";";
  private static String CHARSET_LABEL = "charset";

  private static String NO_CHARSET_FOUND_EX = "illegal charset detected, defaulting to ISO-8859-1";
  private static Gson gson = new Gson();

  @VisibleForTesting
  protected static Charset getCharset(String contentType) {
    String[] tokens = contentType.split(SEMICOL);
    if (tokens.length > 1) {
      for(String token : tokens) {
        String[] charsetParts = token.split(EQ_SEP);
        if (charsetParts[0].toLowerCase().trim().equals(CHARSET_LABEL)) {
          try {
            return Charset.forName(charsetParts[1].trim().toUpperCase());
          } catch (Exception ex) {
            log.error(NO_CHARSET_FOUND_EX, ex);
          }
        }
      }
    }
    return Charsets.ISO_8859_1;
  }

  public static HttpRequestBody parse(byte[] body, String contentType) {
    Map<String, Object> parsedBody;
    Charset charset = getCharset(contentType);
    if (isJson(contentType)) {
      parsedBody = parseAsJson(new String(body, charset));
    } else if (isUrlEncoded(contentType)) {
      parsedBody = parseAsUrlEncoded(new String(body, charset));
    } else {
      parsedBody = null;
    }
    return new HttpRequestBody(body, contentType, parsedBody);
  }
  private static boolean isJson(String contentType) {
    return contentType.equals(HttpHeaderValue.CONTENT_TYPE_JSON);
  }

  private static boolean isUrlEncoded(String contentType) {
    return contentType.equals(HttpHeaderValue.CONTENT_TYPE_URLENCODED);
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
