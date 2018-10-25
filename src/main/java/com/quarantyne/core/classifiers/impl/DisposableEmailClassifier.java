package com.quarantyne.core.classifiers.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.hash.BloomFilter;
import com.quarantyne.core.classifiers.HttpRequestWithBodyClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableEmailClassifier implements HttpRequestWithBodyClassifier {
  private BloomFilter<String> disposableEmailBf;
  private static final String NULL_CHECK = "httpRequest {} or requestBody {} is null";
  private static final String EMAIL_KEY = "email";
  private static final Pattern p = Pattern.compile("@");
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
    if (!Strings.isNullOrEmpty(email)) {
      String[] emailParts = p.split(email);
      if (emailParts.length == 2 && disposableEmailBf.mightContain(emailParts[1])) {
        return Sets.newHashSet(Label.DISPOSABLE_EMAIL);
      }
    }
    return EMPTY_LABELS;
  }

}