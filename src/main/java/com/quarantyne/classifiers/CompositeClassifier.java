package com.quarantyne.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpResponse;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;


public class CompositeClassifier implements HttpRequestClassifier {

  private final List<HttpRequestClassifier> httpRequestClassifiers;

  public CompositeClassifier(
      List<HttpRequestClassifier> httpRequestClassifiers) {
    this.httpRequestClassifiers = httpRequestClassifiers;
  }

  @Override
  public Set<Label> classify(final HttpRequest request, @Nullable HttpRequestBody body) {
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
      @Nullable HttpRequestBody body) {
    httpRequestClassifiers
        .stream()
        .filter(c -> c.test(request, body))
        .forEach(c -> c.record(response, request, body));
  }
}
