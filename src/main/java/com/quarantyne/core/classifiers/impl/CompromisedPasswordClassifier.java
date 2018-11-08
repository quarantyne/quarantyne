package com.quarantyne.core.classifiers.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.hash.BloomFilter;
import com.quarantyne.config.Config;
import com.quarantyne.config.QIdentityAction;
import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompromisedPasswordClassifier implements HttpRequestClassifier {
  private static String NULL_CHECK = "httpRequest {} or requestBody {} is null";
  private static String PASSWORD_KEY = "password";

  private BloomFilter<String> compromisedPasswordBloomFilter;
  private Supplier<Config> config;

  public CompromisedPasswordClassifier(BloomFilter<String> compromisedPasswordBloomFilter,
      Supplier<Config> config) {
    this.config = config;
    this.compromisedPasswordBloomFilter =
        Preconditions.checkNotNull(compromisedPasswordBloomFilter);
  }

  @Override
  public Set<Label> classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (body == null) {
      return EMPTY_LABELS;
    }
    String passwordIdentifier = discoverPasswordIdentifier(
        httpRequest.getPath(),
        config.get().getLoginAction(),
        config.get().getRegisterAction());
    if (!Strings.isNullOrEmpty(passwordIdentifier) &&
        !Strings.isNullOrEmpty(body.get(passwordIdentifier)) &&
        compromisedPasswordBloomFilter.mightContain(body.get(passwordIdentifier))) {
      return Sets.newHashSet(Label.COMPROMISED_PASSWORD);
    }
    return EMPTY_LABELS;
  }

  String discoverPasswordIdentifier(String path, QIdentityAction...actions) {
    for (QIdentityAction action: actions) {
      if (action.isEnabledForPath(path)) {
        return action.getSecretParam();
      }
    }
    return null;
  }

  @Override
  public boolean test(final HttpRequest httpRequest, final HttpRequestBody body) {
    boolean shouldRunOnPath = config.get().getLoginAction().matchesPath(httpRequest.getPath())
        ||  config.get().getRegisterAction().matchesPath(httpRequest.getPath());

    return isWriteRequest(httpRequest)
        && hasBody(body)
        && shouldRunOnPath;
  }
}
