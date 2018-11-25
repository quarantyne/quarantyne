package com.quarantyne.classifiers.impl;

import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LargeBodySizeClassifier implements HttpRequestClassifier {

  protected static int MAX_SIZE_BYTES = 1_024;

  @Override
  public Label classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (body == null) {
      return Label.NONE;
    }
    if (body.getBody().length > MAX_SIZE_BYTES) {
      if (log.isDebugEnabled()) {
        log.debug("{} is sending a large ({} bytes) body size",
            httpRequest.getFingerprint(),
            body.getBody().length);
      }
      return Label.LARGE_BODY;
    }
    return Label.NONE;
  }

  @Override
  public boolean test(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    return isWriteRequest(httpRequest) && hasBody(body);
  }
}
