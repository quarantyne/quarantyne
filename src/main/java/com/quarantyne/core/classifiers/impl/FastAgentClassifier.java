package com.quarantyne.core.classifiers.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.HashCode;
import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.Fingerprinter;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.util.ExponentialBackOff;
import java.time.Duration;
import java.util.Set;

public class FastAgentClassifier implements HttpRequestClassifier {

  private final Cache<HashCode, ExponentialBackOff> penaltyBoxCache;
  private final Cache<HashCode, Boolean> visitCountCache;

  public FastAgentClassifier() {
    this.visitCountCache = Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofMillis(900))
        .build();

    this.penaltyBoxCache = Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofDays(1))
        .build();
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    HashCode id = Fingerprinter.fromString(httpRequest.getRemoteAddress());
    ExponentialBackOff backoff = penaltyBoxCache.getIfPresent(id);
    if (backoff != null) {
      if (backoff.isBackedOff()) {
        backoff.touch();
        return Label.FAST_BROWSER;
      } else {
        penaltyBoxCache.invalidate(backoff);
        return EMPTY_LABELS;
      }
    }
    if (visitCountCache.getIfPresent(id) != null) {
        penaltyBoxCache.put(id, ExponentialBackOff.DEFAULT);
    }
    visitCountCache.put(id, true);
    return EMPTY_LABELS;
  }
}
