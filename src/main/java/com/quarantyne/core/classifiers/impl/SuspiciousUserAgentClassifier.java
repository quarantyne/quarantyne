package com.quarantyne.core.classifiers.impl;

import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SuspiciousUserAgentClassifier implements HttpRequestClassifier {
  private final static String UA = "user-agent";
  protected final static int MIN_LEN = 37;

  @Override
  public Set<Label> classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    String ua = httpRequest.getHeaders().get(UA);
    if (ua == null || ua.length() < MIN_LEN) {
      if (log.isDebugEnabled()) {
        log.debug("suspicious user-agent: {}", ua);
      }
      return Label.SUSPICIOUS_UA;
    }
    return EMPTY_LABELS;
  }
}
