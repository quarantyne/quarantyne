package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.List;
import java.util.Set;


public class CompositeClassifier implements
    HttpRequestClassifier, HttpRequestWithBodyClassifier {

  private final List<HttpRequestClassifier> httpRequestClassifiers;
  private final List<HttpRequestWithBodyClassifier> httpRequestWithBodyClassifiers;

  public CompositeClassifier(
      List<HttpRequestClassifier> httpRequestClassifiers,
      List<HttpRequestWithBodyClassifier> httpRequestWithBodyClassifiers) {
    this.httpRequestClassifiers = httpRequestClassifiers;
    this.httpRequestWithBodyClassifiers = httpRequestWithBodyClassifiers;
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest) {
    Set<Label> labels = Sets.newHashSet();
    httpRequestClassifiers.forEach(c -> {
      labels.addAll(c.classify(httpRequest));
    });
    return labels;
  }

  @Override
  public Set<Label> classify(HttpRequest httpRequest, HttpRequestBody body) {
    Set<Label> labels = Sets.newHashSet();
    httpRequestWithBodyClassifiers.forEach(c -> {
      labels.addAll(c.classify(httpRequest, body));
    });
    return labels;
  }
}
