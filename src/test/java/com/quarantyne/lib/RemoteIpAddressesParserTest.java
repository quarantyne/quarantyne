package com.quarantyne.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Map;
import org.junit.Test;

public class RemoteIpAddressesParserTest {
  private static String IP1 = "1.1.1.1";
  private static String IP2 = "2.2.2.2";
  private static String IP3 = "3.3.3.3";
  @Test
  public void testParseXForwardedFor() {
    Map<String, String> h = Maps.newHashMap();
    h.put("x-forwarded-for", IP1);
    CaseInsensitiveStringKV kv = new CaseInsensitiveStringKV(h);
    RemoteIpAddresses res = RemoteIpAddressesParser.parse(kv, IP2);
    assertThat(res.getOrigin()).isEqualTo(IP1);
    assertThat(res.getProxies()).containsExactlyInAnyOrder(IP2);
  }

  @Test
  public void testParseXForwardedForMulti() {
    Map<String, String> h = Maps.newHashMap();
    h.put("x-forwarded-for", String.format("%s, %s ", IP1, IP2));
    CaseInsensitiveStringKV kv = new CaseInsensitiveStringKV(h);
    RemoteIpAddresses res = RemoteIpAddressesParser.parse(kv, IP3);
    assertThat(res.getOrigin()).isEqualTo(IP1);
    assertThat(res.getProxies()).containsExactlyInAnyOrder(IP2, IP3);
  }

  @Test
  public void testNoProxy() {
    Map<String, String> h = Maps.newHashMap();
    CaseInsensitiveStringKV kv = new CaseInsensitiveStringKV(h);
    RemoteIpAddresses res = RemoteIpAddressesParser.parse(kv, IP1);
    assertThat(res.getOrigin()).isEqualTo(IP1);
    assertThat(res.getProxies()).isEmpty();
  }
}
