package com.quarantyne.classifiers.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.BloomFilter;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.config.Config;
import com.quarantyne.config.QIdentityAction;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import java.util.function.Supplier;
import javax.annotation.Nullable;
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
  public Label classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    if (body == null) {
      return Label.NONE;
    }
    String passwordIdentifier = discoverPasswordIdentifier(
        httpRequest.getPath(),
        config.get().getLoginAction(),
        config.get().getRegisterAction());
    if (!Strings.isNullOrEmpty(passwordIdentifier) &&
        !Strings.isNullOrEmpty(body.get(passwordIdentifier)) &&
        compromisedPasswordBloomFilter.mightContain(body.get(passwordIdentifier))) {
      return Label.COMPROMISED_PASSWORD;
    }
    return Label.NONE;
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
  public boolean test(final HttpRequest httpRequest, @Nullable final HttpRequestBody body) {
    boolean shouldRunOnPath = config.get().getLoginAction().matchesPath(httpRequest.getPath())
        ||  config.get().getRegisterAction().matchesPath(httpRequest.getPath());

    return isWriteRequest(httpRequest)
        && hasBody(body)
        && shouldRunOnPath;
  }
}
