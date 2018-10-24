package com.quarantyne.core.classifiers.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.classifiers.TestHttpRequest;
import com.quarantyne.core.classifiers.TestHttpRequestBody;
import com.quarantyne.core.lib.HttpRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompromisedPasswordClassifierTest {

  @Test
  public void testClassifier() {
    BloomFilter<String> bloom =
        BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 3);
    bloom.put("alpha");
    bloom.put("bravo");
    bloom.put("charlie");

    CompromisedPasswordClassifier classifier = new CompromisedPasswordClassifier(bloom);
    HttpRequest defaultRequest = TestHttpRequest.REQ;

    // null empty
    assertThat(classifier.classify(defaultRequest, null)).isEmpty();
    assertThat(classifier.classify(defaultRequest, TestHttpRequestBody.EMPTY)).isEmpty();

    // no key matches password
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("name", "john")))).isEmpty();

    // a key matches password but password is not in bloomf
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("password", "delta")))).isEmpty();

    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("password", "bravo")))).isEqualTo(
            Label.COMPROMISED_PASSWORD);
  }
}
