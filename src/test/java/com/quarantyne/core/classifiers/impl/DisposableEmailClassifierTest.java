package com.quarantyne.core.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.quarantyne.config.Config;
import com.quarantyne.config.QIdentityAction;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.classifiers.TestHttpRequest;
import com.quarantyne.core.classifiers.TestHttpRequestBody;
import com.quarantyne.core.lib.HttpRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class DisposableEmailClassifierTest extends AbstractClassifierTest {

  @Test
  public void testClassifier() {
    BloomFilter<String> bloom =
        BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 2);
    bloom.put("disposable.com");
    bloom.put("junk.com");

    Supplier<Config> configSupplier = () -> Config
        .builder()
        .emailParamKeys(Sets.newHashSet("email"))
        .registerAction(new QIdentityAction("/register", "email", "password"))
        .build();
    DisposableEmailClassifier classifier = new DisposableEmailClassifier(bloom, configSupplier);
    HttpRequest defaultRequest = TestHttpRequest.REQ();

    // null empty
    assertThat(classifier.classify(defaultRequest, null)).isEmpty();
    assertThat(classifier.classify(defaultRequest, TestHttpRequestBody.EMPTY)).isEmpty();

    // no key matches password
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("name", "john")))).isEmpty();

    // a key matches password but password is not in bloomf
    assertThat(classifier.classify(defaultRequest,
        TestHttpRequestBody.make(new JsonObject().put("email", "john@gmail.com")))).isEmpty();

    // match
    HttpRequest req = new TestHttpRequest.Builder().setPath("/register").build();
    assertThat(classifier.classify(req,
        TestHttpRequestBody.make(new JsonObject().put("email", "spammy@disposable.com")))).isEqualTo(
        Label.DISPOSABLE_EMAIL);
  }
}
