package com.quarantyne.classifiers.impl;

import com.google.common.base.Strings;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.config.Config;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoName;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeoDiscrepancyClassifier implements HttpRequestClassifier {

  private final GeoIp4j geoIp4j;
  private final Supplier<Config> config;

  public GeoDiscrepancyClassifier(GeoIp4j geoIp4j,
      Supplier<Config> config) {
    this.geoIp4j = geoIp4j;
    this.config = config;
  }

  @Override
  public Label classify(HttpRequest httpRequest, HttpRequestBody body) {
    if (body == null) {
      return Label.NONE;
    }
    String isoCountryCode = body.getAny(config.get().getCountryIsoCodeParamKeys());
    if (!Strings.isNullOrEmpty(isoCountryCode)) {
      Optional<GeoName> geoName = geoIp4j.getGeoName(httpRequest.getRemoteIpAddresses().getOrigin());
      if (geoName.isPresent() &&
          geoName.get().getIsoCode().toLowerCase().equalsIgnoreCase(isoCountryCode)) {
        log.debug("{} is using country {} which is different from its IP address' country {}",
            httpRequest.getFingerprint(),
            isoCountryCode,
            geoName.get().getIsoCode());

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
