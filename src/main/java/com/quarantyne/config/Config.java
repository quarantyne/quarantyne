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

  final QIdentityAction loginAction;
  final QIdentityAction registerAction;
  final Set<String> emailParamKeys;
  final Set<String> countryIsoCodeParamKeys;
  final String blockedRequestPage;
  final Set<Label> blockedClasses;
  final boolean isDisabled;

  public Config() {
    Set<String> emptySet = Sets.newHashSet();
    this.loginAction = new QIdentityAction("/login", "email", "password");
    this.registerAction = new QIdentityAction("/register", "email", "password");
    this.emailParamKeys = emptySet;
    this.countryIsoCodeParamKeys = emptySet;
    this.blockedRequestPage = "https://raw.githubusercontent.com/AndiDittrich/HttpErrorPages/master/dist/HTTP500.html";
    this.blockedClasses = Sets.newHashSet();
    this.isDisabled = false;
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
    return Label.ALL.contains(label);
  }

  public boolean isBlocked(Set<Label> labels) {
    System.out.println(labels);
    return ! Sets.intersection(labels, Label.ALL).isEmpty();
  }
}
