package com.quarantyne.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;


public class MainClassifier {

  private final List<HttpRequestClassifier> httpRequestClassifiers;

  public MainClassifier(
      List<HttpRequestClassifier> httpRequestClassifiers) {
    this.httpRequestClassifiers = httpRequestClassifiers;
  }

  public Set<Label> classify(final HttpRequest request, @Nullable HttpRequestBody body) {
    return httpRequestClassifiers
        .stream()
        .filter(c -> c.test(request, body))
        .map(c -> c.classify(request, body))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  public void record(HttpResponse response, HttpRequest request,
      @Nullable HttpRequestBody body) {
    httpRequestClassifiers
        .stream()
        .filter(c -> c.test(request, body))
        .forEach(c -> c.record(response, request, body));
  }
}
