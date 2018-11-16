package com.quarantyne.classifiers.impl;

import com.google.common.base.Strings;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.config.Config;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoName;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpResponse;
import com.quarantyne.login_history.LoginEvent;
import com.quarantyne.login_history.LoginHistoryStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class SuspiciousLoginActivityClassifier implements HttpRequestClassifier {
  private static String LOGIN_KEY = "login";
  private final GeoIp4j geoIp4j;
  private final Supplier<Config> config;
  private final LoginHistoryStore loginHistoryStore;

  public SuspiciousLoginActivityClassifier(GeoIp4j geoIp4j,
      Supplier<Config> config,
      LoginHistoryStore loginHistoryStore) {
    this.geoIp4j = geoIp4j;
    this.config = config;
    this.loginHistoryStore = loginHistoryStore;
  }

  @Override
  public Set<Label> classify(HttpRequest httpRequest, HttpRequestBody body) {
    if (body == null) {
      return EMPTY_LABELS;
    }
    String loginIdentifier = body.get( config.get().getLoginAction().getIdentifierParam());
    if (Strings.isNullOrEmpty(loginIdentifier)) {
      String thisLoginCountry = geoIp4j
          .getGeoName(httpRequest.getRemoteAddress())
          .map(GeoName::getIsoCode)
          .orElse(null);
      LoginEvent loginEvent = new LoginEvent(
          Instant.now(),
          httpRequest.getRemoteAddress(),
          thisLoginCountry
      );
      // TODO switch to last two countries
      List<LoginEvent> loginEvents = loginHistoryStore.getLastLogins(loginIdentifier, 10);
      // pass if not enough data
      if (loginEvents.size() >= 3 && !getTopLoginCountry(loginEvents).contains(thisLoginCountry)) {
        return Label.SUSPICIOUS_GEO;
      }
    }
    return Label.NONE;
  }

  static Set<String> getTopLoginCountry(List<LoginEvent> loginEvents) {
    HashMap<String, Integer> counter = new HashMap<>();
    String k;
    for (LoginEvent loginEvent: loginEvents) {
      k = loginEvent.getCountryIsoCode();
      if (counter.containsKey(k)) {
        counter.put(k, counter.get(k) + 1);
      } else {
        counter.put(k, 1);
      }
    }
    return null;
  }

  // record a successful login
  @Override
  public void record(HttpResponse response, HttpRequest httpRequest,
      HttpRequestBody body) {
    String loginIdentifier = body.get(config.get().getLoginAction().getIdentifierParam());
    if (!Strings.isNullOrEmpty(loginIdentifier)) {
      loginHistoryStore.registerLogin(loginIdentifier,
          new LoginEvent(
              Instant.now(),
              httpRequest.getRemoteAddress(),
              getIsoCode(geoIp4j.getGeoName(httpRequest.getRemoteAddress()))));
    }
  }

  String getIsoCode(Optional<GeoName> geoNameOpt) {
    return geoNameOpt.map(GeoName::getIsoCode).orElse(null);
  }

  @Override
  public boolean test(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    return isWriteRequest(httpRequest)
        && hasBody(body)
        && config.get().getLoginAction().matchesPath(httpRequest.getPath());
  }
}
