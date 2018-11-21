package com.quarantyne.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.quarantyne.classifiers.Label;
import com.quarantyne.classifiers.TestHttpRequest;
import com.quarantyne.classifiers.TestHttpRequestBody;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class LargeBodySizeClassifierTest {

  @Test
  public void testClassifier() {
    LargeBodySizeClassifier classifier = new LargeBodySizeClassifier();
    HttpRequest req = TestHttpRequest.REQ();
    HttpRequestBody body = TestHttpRequestBody.EMPTY;
    assertThat(classifier.classify(req, body)).isEqualTo(Label.NONE);
    JsonObject largePayload =
        new JsonObject().put("k",new String(new byte[LargeBodySizeClassifier.MAX_SIZE_BYTES]));
    assertThat(classifier.classify(req, TestHttpRequestBody.make(largePayload)))
        .isEqualTo(Label.LARGE_BODY);
  }

}
