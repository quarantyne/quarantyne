package com.quarantyne.core.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HttpHeaders;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.classifiers.TestHttpRequest;
import com.quarantyne.core.lib.HttpRequest;
import org.junit.Test;

public class SuspiciousUserAgentClassifierTest {

  @Test
  public void testClassifier() {
    SuspiciousUserAgentClassifier classifier = new SuspiciousUserAgentClassifier();
    HttpRequest req = TestHttpRequest.REQ();
    assertThat(classifier.classify(req)).isEqualTo(Label.NONE);
    req.getHeaders().replace(HttpHeaders.USER_AGENT, "curl");
    assertThat(classifier.classify(req)).isEqualTo(Label.SUSPICIOUS_UA);
  }
}
