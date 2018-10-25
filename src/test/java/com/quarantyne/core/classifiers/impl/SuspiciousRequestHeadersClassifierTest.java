package com.quarantyne.core.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.classifiers.TestHttpRequest;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.util.CaseInsensitiveStringKV;
import java.util.Map;
import org.junit.Test;

public class SuspiciousRequestHeadersClassifierTest {

  @Test
  public void testClassifier() {
    SuspiciousRequestHeadersClassifier classifier = new SuspiciousRequestHeadersClassifier();
    HttpRequest req = TestHttpRequest.REQ();
    assertThat(classifier.classify(req)).isEmpty();
    Map<String, String> smallHeaders = Maps.newHashMap();
    smallHeaders.put(HttpHeaders.HOST, "example.com");
    smallHeaders.put(HttpHeaders.CONNECTION, "close");
    HttpRequest reqSmallHeaders =
        new TestHttpRequest.Builder().setHeaders(new CaseInsensitiveStringKV(smallHeaders)).build();
    assertThat(classifier.classify(reqSmallHeaders)).isEqualTo(Label.SUSPICIOUS_HEADERS);
  }
}
