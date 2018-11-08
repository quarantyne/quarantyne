package com.quarantyne.config;


import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
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
  @Nullable  final String blockedRequestPage;
  final Set<String> blockedClasses;
  final boolean isDisabled;

  public Config() {
    Set<String> emptySet = Sets.newHashSet();
    this.loginAction = new QIdentityAction("/login", "email", "password");
    this.registerAction = new QIdentityAction("/register", "email", "password");
    this.emailParamKeys = emptySet;
    this.countryIsoCodeParamKeys = emptySet;
    this.blockedRequestPage = null;
    this.blockedClasses = emptySet;
    this.isDisabled = false;
  }

  Config(QIdentityAction loginAction, QIdentityAction registerAction,
      Set<String> emailParamKeys, Set<String> countryIsoCodeParamKeys,
      String blockedRequestPage, Set<String> blockedClasses, boolean isDisabled) {
    this.loginAction = loginAction;
    this.registerAction = registerAction;
    this.emailParamKeys = emailParamKeys;
    this.countryIsoCodeParamKeys = countryIsoCodeParamKeys;
    this.blockedRequestPage = blockedRequestPage;
    this.blockedClasses = blockedClasses;
    this.isDisabled = isDisabled;
  }


}
