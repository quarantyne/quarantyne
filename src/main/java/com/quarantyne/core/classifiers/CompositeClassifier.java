package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.lib.HttpResponse;
import java.util.List;
import java.util.Set;


public class CompositeClassifier implements HttpRequestClassifier {

  private final List<HttpRequestClassifier> httpRequestClassifiers;

  public CompositeClassifier(
      List<HttpRequestClassifier> httpRequestClassifiers) {
    this.httpRequestClassifiers = httpRequestClassifiers;
  }

  @Override
  public Set<Label> classify(final HttpRequest request, HttpRequestBody body) {
    Set<Label> labels = Sets.newHashSet();
    httpRequestClassifiers
        .stream()
        .filter(c -> c.test(request, body)).forEach(c -> {
      labels.addAll(c.classify(request, body));
    });
    return labels;
  }

  @Override
  public void record(HttpResponse response, HttpRequest request,
      HttpRequestBody body) {
    httpRequestClassifiers
        .stream()
        .filter(c -> c.test(request, body))
        .forEach(c -> c.record(response, request, body));
  }
}
