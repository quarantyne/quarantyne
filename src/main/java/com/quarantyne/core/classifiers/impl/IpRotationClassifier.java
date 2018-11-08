package com.quarantyne.core.classifiers.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.Fingerprinter;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.time.Duration;
import java.util.Set;

/**
 * Assumes that, in a 10 seconds window, two identical sets of headers are from the same agent
 * even if their IP is different
 */
public class IpRotationClassifier implements HttpRequestClassifier {
  private final Cache<HashCode, String> lastSeenCache;

  public IpRotationClassifier() {
    this.lastSeenCache = CacheBuilder
        .newBuilder()
        .expireAfterAccess(Duration.ofSeconds(10))
        .build();
  }

  @Override
  public Set<Label> classify(HttpRequest httpRequest, HttpRequestBody body) {
    String requestIp = httpRequest.getRemoteAddress();
    HashCode headersHashcode = Fingerprinter.fromHeaders(httpRequest.getHeaders());
    String seenIp = lastSeenCache.getIfPresent(headersHashcode);
    if (seenIp != null && !requestIp.equals(seenIp)) {
      return Label.IP_ROTATION;
    }

    // rotate last seen ip in cache too
    lastSeenCache.put(headersHashcode, requestIp);
    return EMPTY_LABELS;
  }

}
