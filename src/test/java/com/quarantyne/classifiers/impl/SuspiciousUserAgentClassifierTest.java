package com.quarantyne.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HttpHeaders;
import com.quarantyne.classifiers.Label;
import com.quarantyne.classifiers.TestHttpRequest;
import com.quarantyne.lib.HttpRequest;
import org.junit.Test;

public class SuspiciousUserAgentClassifierTest {

  @Test
  public void testClassifier() {
    SuspiciousUserAgentClassifier classifier = new SuspiciousUserAgentClassifier();
    HttpRequest req = TestHttpRequest.REQ();
    assertThat(classifier.classify(req, null)).isEqualTo(Label.NONE);
    req.getHeaders().replace(HttpHeaders.USER_AGENT, "curl");
    assertThat(classifier.classify(req, null)).isEqualTo(Label.SUSPICIOUS_UA);
  }
}
