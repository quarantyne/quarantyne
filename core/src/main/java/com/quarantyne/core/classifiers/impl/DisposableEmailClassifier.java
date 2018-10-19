package com.quarantyne.core.classifiers.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.hash.BloomFilter;
import com.quarantyne.core.classifiers.HttpRequestWithBodyClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableEmailClassifier implements HttpRequestWithBodyClassifier {
  private BloomFilter<String> disposableEmailBf;
  private static String NULL_CHECK = "httpRequest {} or requestBody {} is null";
  private static String EMAIL_KEY = "email";

  public DisposableEmailClassifier(BloomFilter<String> disposableEmailBf) {
    this.disposableEmailBf = disposableEmailBf;
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (httpRequest == null || body == null) {
      log.warn(NULL_CHECK, httpRequest, body);
      return EMPTY_LABELS;
    }

    final String email = body.get(EMAIL_KEY);
    if (!Strings.isNullOrEmpty(email) && disposableEmailBf.mightContain(email)) {
      return Sets.newHashSet(Label.DISPOSABLE_EMAIL);
    }
    return EMPTY_LABELS;
  }

}