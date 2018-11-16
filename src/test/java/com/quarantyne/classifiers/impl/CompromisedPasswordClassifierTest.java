package com.quarantyne.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.quarantyne.config.Config;
import com.quarantyne.config.QIdentityAction;
import com.quarantyne.classifiers.Label;
import com.quarantyne.classifiers.TestHttpRequest;
import com.quarantyne.classifiers.TestHttpRequestBody;
import com.quarantyne.lib.HttpRequest;
import io.vertx.core.json.JsonObject;
import java.util.function.Supplier;
import org.junit.Test;

public class CompromisedPasswordClassifierTest extends AbstractClassifierTest {


  @Test
  public void testClassifier() {
    BloomFilter<String> bloom =
        BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 3);
    bloom.put("alpha");
    bloom.put("bravo");
    bloom.put("charlie");

    Supplier<Config> configSupplier = () -> Config.builder()
        .loginAction(new QIdentityAction("/login", "email", "password"))
        .registerAction(new QIdentityAction("/register", "email", "password"))
        .build();
    CompromisedPasswordClassifier classifier = new CompromisedPasswordClassifier(bloom, configSupplier);
    HttpRequest defaultRequest = TestHttpRequest.REQ();

    // null empty
    assertThat(classifier.classify(defaultRequest, null)).isEmpty();
    assertThat(classifier.classify(defaultRequest, TestHttpRequestBody.EMPTY)).isEmpty();

    // no key matches password
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("name", "john")))).isEmpty();

    // a key matches password but password is not in bloomf
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("password", "delta")))).isEmpty();

    // match
    HttpRequest requestOnPath = new TestHttpRequest.Builder().setPath("/login").build();
    assertThat(classifier.classify(requestOnPath,
        TestHttpRequestBody.make(new JsonObject().put("password", "bravo")))).isEqualTo(
            Label.COMPROMISED_PASSWORD);
  }
}
