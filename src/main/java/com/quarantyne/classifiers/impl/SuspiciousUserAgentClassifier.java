package com.quarantyne.classifiers.impl;

import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SuspiciousUserAgentClassifier implements HttpRequestClassifier {
  private final static String UA = "user-agent";
  protected final static int MIN_LEN = 37;

  @Override
  public Label classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    String ua = httpRequest.getHeaders().get(UA);
    if (ua == null || ua.length() < MIN_LEN) {
      if (log.isDebugEnabled()) {
        log.debug("{} user-agent ({}) is suspicious", httpRequest.getFingerprint(), ua);
      }
      return Label.SUSPICIOUS_UA;
    }
    return Label.NONE;
  }
}
