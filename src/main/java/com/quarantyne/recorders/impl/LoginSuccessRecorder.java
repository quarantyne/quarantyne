package com.quarantyne.recorders.impl;

import com.quarantyne.config.Config;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoName;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpResponse;
import com.quarantyne.login_history.LoginEvent;
import com.quarantyne.login_history.LoginHistoryStore;
import com.quarantyne.recorders.HttpResponseRecorder;
import java.time.Instant;
import java.util.function.Supplier;

public class LoginSuccessRecorder implements HttpResponseRecorder {

  private final Supplier<Config> config;
  private final LoginHistoryStore loginHistoryStore;
  private final GeoIp4j geoIp4j;

  public LoginSuccessRecorder(Supplier<Config> config,
      LoginHistoryStore loginHistoryStore, GeoIp4j geoIp4j) {
    this.config = config;
    this.loginHistoryStore = loginHistoryStore;
    this.geoIp4j = geoIp4j;
  }

  @Override
  public void record(HttpRequest request, HttpRequestBody httpRequestBody, HttpResponse response) {
    String identifier = null;

    loginHistoryStore.registerLogin(identifier, new LoginEvent(
        Instant.now(),
        request.getRemoteAddress(),
        geoIp4j.getGeoName(request.getRemoteAddress()).map(GeoName::getIsoCode).orElse(null))
    );
  }

  @Override
  public boolean test(HttpRequest request, HttpRequestBody body, HttpResponse response) {
    return 200 <= response.getStatusCode()
        && response.getStatusCode() < 300
        && config.get().getLoginAction().matchesPath(request.getPath())
        && isWriteRequest(request)
        && hasBody(body);
  }
}
