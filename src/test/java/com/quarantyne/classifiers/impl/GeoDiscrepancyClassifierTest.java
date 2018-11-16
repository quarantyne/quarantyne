package com.quarantyne.classifiers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quarantyne.classifiers.Label;
import com.quarantyne.classifiers.TestHttpRequest;
import com.quarantyne.classifiers.TestHttpRequestBody;
import com.quarantyne.config.Config;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoName;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

public class GeoDiscrepancyClassifierTest extends AbstractClassifierTest {
  private GeoIp4j geoIp4j = mock(GeoIp4j.class);
  private Config config;
  private GeoDiscrepancyClassifier classifier = new GeoDiscrepancyClassifier(geoIp4j, () -> config);

  @Before
  public void setup() {
    config = new Config();
    when(geoIp4j.getGeoName(anyString()))
        .thenReturn(Optional.of(new GeoName("cc", "countryCode")));
  }

  @Test
  public void testCountryFound() {
    config = Config.builder().countryIsoCodeParamKeys(Sets.newLinkedHashSet("country_iso_code")).build();
    JsonObject body = new JsonObject();
    body.put("country_iso_code", "cc");
    assertThat(
        classifier.classify(
            TestHttpRequest.REQ(),
            TestHttpRequestBody.make(body)))
        .isEqualTo(Label.IP_COUNTRY_DISCREPANCY);
  }

  @Test
  public void testCountryNotFound() {
    JsonObject body = new JsonObject();
    body.put("test", new JsonObject());
    assertThat(
        classifier.classify(
            TestHttpRequest.REQ(),
            TestHttpRequestBody.make(body)))
        .isEqualTo(Label.NONE);
  }
}
