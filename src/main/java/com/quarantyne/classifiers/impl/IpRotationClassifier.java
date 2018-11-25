package com.quarantyne.classifiers.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.Fingerprinter;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.RemoteIpAddresses;
import java.time.Duration;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Assumes that, in a 10 seconds window, two identical sets of headers are from the same agent
 * even if their IP is different
 */
@Slf4j
public class IpRotationClassifier implements HttpRequestClassifier {
  private final Cache<HashCode, RemoteIpAddresses> lastSeenCache;

  public IpRotationClassifier() {
    this.lastSeenCache = CacheBuilder
        .newBuilder()
        .expireAfterAccess(Duration.ofSeconds(10))
        .build();
  }

  @Override
  public Label classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    RemoteIpAddresses requestIp = httpRequest.getRemoteIpAddresses();
    HashCode headersHashcode = Fingerprinter.fromHeaders(httpRequest.getHeaders());
    RemoteIpAddresses seenIp = lastSeenCache.getIfPresent(headersHashcode);
    if (seenIp != null && !requestIp.getOrigin().equals(seenIp.getOrigin())) {
      log.debug("request ip {} / seen ip {} / headers hashcode {}", requestIp, seenIp, headersHashcode);
      return Label.IP_ROTATION;
    }

    // rotate last seen ip in cache too
    lastSeenCache.put(headersHashcode, requestIp);
    return Label.NONE;
  }

}
