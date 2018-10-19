package com.quarantyne.core.lib;


import com.google.common.collect.Maps;
import com.quarantyne.core.util.CaseInsensitiveStringKV;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FingerprinterTest {
  @Test
  public void testStringFingerprintingEquality() {
    assertThat(Fingerprinter.fromString("1.2.3.4")).isEqualTo(Fingerprinter.fromString("1.2.3.4"));
  }

  @Test
  public void testHeaderFingerprintingEquality() {
    Map<String, String> headers = Maps.newHashMap();
    headers.put("cookie", "a=b");
    headers.put("user-agent", "hello firefox");
    CaseInsensitiveStringKV qHeaders = new CaseInsensitiveStringKV(headers);
    assertThat(Fingerprinter.fromHeaders(qHeaders)).isEqualTo(Fingerprinter.fromHeaders(qHeaders));

    Map<String, String> headersDifferentOrder = Maps.newHashMap();
    headersDifferentOrder.put("user-agent", "hello firefox");
    headersDifferentOrder.put("cookie", "a=b");
    CaseInsensitiveStringKV qHeadersDifferentOrder = new CaseInsensitiveStringKV(headersDifferentOrder);
    assertThat(Fingerprinter.fromHeaders(qHeadersDifferentOrder)).isEqualTo(Fingerprinter.fromHeaders(qHeadersDifferentOrder));

    Map<String, String> headersDifferentCase = Maps.newHashMap();
    headersDifferentCase.put("Cookie", "a=b");
    headersDifferentCase.put("User-Agent", "hello firefox");
    CaseInsensitiveStringKV qheadersDifferentCase = new CaseInsensitiveStringKV(headersDifferentCase);
    assertThat(Fingerprinter.fromHeaders(qheadersDifferentCase)).isEqualTo(Fingerprinter.fromHeaders(qheadersDifferentCase));

    Map<String, String> headersDifferentValue = Maps.newHashMap();
    headersDifferentValue.put("Cookie", "a=c");
    headersDifferentValue.put("User-Agent", "hello firefox");
    CaseInsensitiveStringKV qheadersDifferentValue = new CaseInsensitiveStringKV(headersDifferentValue);
    assertThat(Fingerprinter.fromHeaders(qHeaders)).isNotEqualTo(Fingerprinter.fromHeaders(qheadersDifferentValue));
  }

}
