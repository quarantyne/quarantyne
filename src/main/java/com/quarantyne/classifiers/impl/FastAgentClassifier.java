package com.quarantyne.classifiers.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.HashCode;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.Fingerprinter;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.util.ExponentialBackOff;
import java.time.Duration;
import java.util.Set;
import javax.annotation.Nullable;

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
        .expireAfterWrite(Duration.ofMinutes(20))
        .build();
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, @Nullable final HttpRequestBody body) {
    HashCode id = Fingerprinter.fromString(httpRequest.getId());
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
