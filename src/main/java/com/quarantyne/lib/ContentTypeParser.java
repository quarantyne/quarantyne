package com.quarantyne.lib;

import com.google.common.base.Strings;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContentTypeParser {
  private static String SEP = ";";
  private static String EQ = "=";

  static ContentType parse(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    value = value.toLowerCase();

    if (value.contains(SEP)) {
      String[] parts = value.split(SEP);
      Charset charset = null;
      if (parts[1].contains(EQ)) {
        String[] charsetParts = parts[1].split(EQ);
        try {
          charset = Charset.forName(charsetParts[1].trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
          log.error("cannot parse charset " + parts[1], ex);
        }
      }

      return new ContentType(parts[0], charset);
    } else {
      return new ContentType(value.trim(), null);
    }
  }
}
