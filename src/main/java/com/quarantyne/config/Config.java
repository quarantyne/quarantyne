package com.quarantyne.config;


import com.google.common.collect.Sets;
import com.quarantyne.classifiers.Label;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * Quarantyne configuration.
 * Configuration is optional and null unless set.
 */
@Data
@Builder
public class Config {
  final static Set<String> EMPTY_SET = Sets.newHashSet();
  final QIdentityAction loginAction;
  final QIdentityAction registerAction;
  final Set<String> emailParamKeys;
  final Set<String> countryIsoCodeParamKeys;
  final String blockedRequestPage;
  final Set<Label> blockedClasses;
  final boolean isDisabled;

  public Config() {
    this(new QIdentityAction("/login", "email", "password"),
        new QIdentityAction("/register", "email", "password"),
        EMPTY_SET,
        EMPTY_SET,
        "https://raw.githubusercontent.com/AndiDittrich/HttpErrorPages/master/dist/HTTP500.html",
        Sets.newHashSet(Label.FAST_BROWSER),
        false);
  }

  Config(QIdentityAction loginAction, QIdentityAction registerAction,
      Set<String> emailParamKeys, Set<String> countryIsoCodeParamKeys,
      String blockedRequestPage, Set<Label> blockedClasses, boolean isDisabled) {
    this.loginAction = loginAction;
    this.registerAction = registerAction;
    this.emailParamKeys = emailParamKeys;
    this.countryIsoCodeParamKeys = countryIsoCodeParamKeys;
    this.blockedRequestPage = blockedRequestPage;
    this.blockedClasses = blockedClasses;
    this.isDisabled = isDisabled;
  }

  public boolean isBlocked(Label label) {
    return label.equals(Label.ALL) || blockedClasses.contains(label);
  }

  public boolean isBlocked(Set<Label> labels) {
    return !labels.isEmpty() &&
        (blockedClasses.contains(Label.ALL) ||
        ! Sets.intersection(labels, blockedClasses).isEmpty());
  }
}
