package com.quarantyne.classifiers.impl;

import com.google.common.base.Strings;
import com.quarantyne.config.Config;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoName;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class GeoDiscrepancyClassifier implements HttpRequestClassifier {

  private final GeoIp4j geoIp4j;
  private final Supplier<Config> config;

  public GeoDiscrepancyClassifier(GeoIp4j geoIp4j,
      Supplier<Config> config) {
    this.geoIp4j = geoIp4j;
    this.config = config;
  }

  @Override
  public Set<Label> classify(HttpRequest httpRequest, HttpRequestBody body) {
    if (body == null) {
      return EMPTY_LABELS;
    }
    String isoCountryCode = body.getAny(config.get().getCountryIsoCodeParamKeys());
    if (!Strings.isNullOrEmpty(isoCountryCode)) {
      Optional<GeoName> geoName = geoIp4j.getGeoName(httpRequest.getRemoteAddress());
      if (geoName.isPresent() &&
          geoName.get().getIsoCode().toLowerCase().equalsIgnoreCase(isoCountryCode)) {
        return Label.IP_COUNTRY_DISCREPANCY;
      }
    }
    return Label.NONE;
  }

  @Override
  public boolean test(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    return isWriteRequest(httpRequest) && hasBody(body);
  }
}
