package com.quarantyne.classifiers.impl;

import com.google.common.base.Strings;
import com.google.common.hash.BloomFilter;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.config.Config;
import com.quarantyne.config.QIdentityAction;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableEmailClassifier implements HttpRequestClassifier {
  private BloomFilter<String> disposableEmailBf;
  private Supplier<Config> config;

  private static final Pattern p = Pattern.compile("@");

  public DisposableEmailClassifier(BloomFilter<String> disposableEmailBf, Supplier<Config> config) {
    this.config = config;
    this.disposableEmailBf = disposableEmailBf;
  }

  @Override
  public Label classify(final HttpRequest httpRequest, final HttpRequestBody body) {
    String email = null;

    // check for registration or just about any write
    if (body != null) {
      QIdentityAction registerAction = config.get().getRegisterAction();
      if (registerAction.isEnabledForPath(httpRequest.getPath())) {
        email = body.get(registerAction.getIdentifierParam());
      } else {
        email = body.getAny(config.get().getEmailParamKeys());
      }
    }

    if (!Strings.isNullOrEmpty(email)) {
      String[] emailParts = p.split(email, 2);
      if (emailParts.length == 2 && disposableEmailBf.mightContain(emailParts[1])) {
        return Label.DISPOSABLE_EMAIL;
      }
    }
    return Label.NONE;
  }


  @Override
  public boolean test(final HttpRequest httpRequest, @Nullable final HttpRequestBody body) {
    return isWriteRequest(httpRequest)
        && hasBody(body);
  }
}