package com.quarantyne.core.classifiers.impl;

import com.quarantyne.core.classifiers.HttpRequestWithBodyClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LargeBodySizeClassifier implements HttpRequestWithBodyClassifier {

  private static int MAX_SIZE_BYTES = 1_024 * 1000;

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (body.getBody().length > MAX_SIZE_BYTES) {
      if (log.isDebugEnabled()) {
        log.debug("large body size: {} bytes", body.getBody().length);
      }
      return Label.LARGE_BODY;
    }
    return EMPTY_LABELS;
  }
}
