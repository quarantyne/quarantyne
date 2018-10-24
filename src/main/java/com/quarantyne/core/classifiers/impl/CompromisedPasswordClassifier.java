package com.quarantyne.core.classifiers.impl;

import com.google.common.annotations.VisibleForTesting;
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
public class CompromisedPasswordClassifier implements HttpRequestWithBodyClassifier {
  private BloomFilter<String> compromisedPasswordBloomFilter;
  private static String NULL_CHECK = "httpRequest {} or requestBody {} is null";
  private static String PASSWORD_KEY = "password";

  @VisibleForTesting
  static Set<String> passwordFieldKey = Sets.newHashSet("password", "pass", "pw");

  public CompromisedPasswordClassifier(BloomFilter<String> compromisedPasswordBloomFilter) {
    this.compromisedPasswordBloomFilter = compromisedPasswordBloomFilter;
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (httpRequest == null || body == null) {
      log.warn(NULL_CHECK, httpRequest, body);
      return EMPTY_LABELS;
    }

    final String password = body.get(PASSWORD_KEY);
    if (!Strings.isNullOrEmpty(password) && compromisedPasswordBloomFilter.mightContain(password)) {
      return Sets.newHashSet(Label.COMPROMISED_PASSWORD);
    }
    return EMPTY_LABELS;
  }

}
