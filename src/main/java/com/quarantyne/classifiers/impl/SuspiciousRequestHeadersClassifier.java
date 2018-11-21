package com.quarantyne.classifiers.impl;

import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SuspiciousRequestHeadersClassifier implements HttpRequestClassifier {

  @Override
  public Label classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    if (httpRequest.getHeaders().size() <= 5) {
      if (log.isDebugEnabled()) {
        log.debug("suspicious headers: {}", httpRequest.getHeaders());
      }
      return Label.SUSPICIOUS_HEADERS;
    }
    return Label.NONE;
  }

}
