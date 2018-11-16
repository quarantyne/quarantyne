package com.quarantyne.config;

import lombok.Value;

@Value
public class EgressUrl {
  private static String HTTPS = "https";
  String scheme;
  String host;
  int port;
  public boolean isSsl() {
    return scheme.equals(HTTPS);
  }
  public String toHuman() {
    return String.format("%s://%s:%s", scheme, host, port);
  }
}
