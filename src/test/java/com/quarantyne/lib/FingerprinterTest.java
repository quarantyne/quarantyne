package com.quarantyne.lib;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Map;
import org.junit.Test;

public class FingerprinterTest {
  @Test
  public void testStringFingerprintingEquality() {
    assertThat(Fingerprinter.fromString("1.2.3.4")).isEqualTo(Fingerprinter.fromString("1.2.3.4"));
  }

  @Test
  public void testHeaderFingerprintingEquality() {
    Map<String, String> headers = Maps.newHashMap();
    headers.put("user-agent", "hello firefox");
    headers.put("accept", "text/html");
    CaseInsensitiveStringKV qHeaders = new CaseInsensitiveStringKV(headers);
    assertThat(Fingerprinter.fromHeaders(qHeaders)).isEqualTo(Fingerprinter.fromHeaders(qHeaders));

    Map<String, String> headersDifferentOrder = Maps.newHashMap();
    headersDifferentOrder.put("user-agent", "hello firefox");
    headersDifferentOrder.put("accept", "text/html");
    CaseInsensitiveStringKV qHeadersDifferentOrder = new CaseInsensitiveStringKV(headersDifferentOrder);
    assertThat(Fingerprinter.fromHeaders(qHeadersDifferentOrder)).isEqualTo(Fingerprinter.fromHeaders(qHeadersDifferentOrder));

    Map<String, String> headersDifferentCase = Maps.newHashMap();
    headersDifferentCase.put("accept", "text/html");
    headersDifferentCase.put("User-Agent", "hello firefox");
    CaseInsensitiveStringKV qheadersDifferentCase = new CaseInsensitiveStringKV(headersDifferentCase);
    assertThat(Fingerprinter.fromHeaders(qheadersDifferentCase)).isEqualTo(Fingerprinter.fromHeaders(qheadersDifferentCase));

    Map<String, String> headersDifferentValue = Maps.newHashMap();
    headersDifferentValue.put("accept", "application/json");
    headersDifferentValue.put("User-Agent", "hello firefox");
    CaseInsensitiveStringKV qheadersDifferentValue = new CaseInsensitiveStringKV(headersDifferentValue);
    assertThat(Fingerprinter.fromHeaders(qHeaders)).isNotEqualTo(Fingerprinter.fromHeaders(qheadersDifferentValue));
  }

}
