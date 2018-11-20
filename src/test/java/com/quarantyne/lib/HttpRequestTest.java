package com.quarantyne.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Map;
import org.junit.Test;

public class HttpRequestTest {

  @Test
  public void testNotEquals() {
    Map <String, String> h1 = Maps.newHashMap();
    h1.put("k", "v");
    h1.put("x", "y");
    h1.put("user-agent", "chrome");
    CaseInsensitiveStringKV H1 = new CaseInsensitiveStringKV(h1);

    Map <String, String> h2 = Maps.newHashMap();
    h1.put("x", "y");
    h1.put("user-agent", "firefox");
    CaseInsensitiveStringKV H2 = new CaseInsensitiveStringKV(h2);

    HttpRequest R1 = new HttpRequest(
        HttpRequestMethod.GET,
        H1,
        new RemoteIpAddresses("1.2.3.4", Sets.newHashSet("2.2.2.2", "3.3.3.3")),
        "/"
    );

    HttpRequest R2 = new HttpRequest(
        HttpRequestMethod.POST,
        H2,
        new RemoteIpAddresses("1.2.3.4", Sets.newHashSet("2.2.2.2", "3.3.3.3")),
        "/abc"
    );

    assertThat(R1).isNotEqualTo(R2);
  }

  @Test
  public void testEquals() {
    Map <String, String> h1 = Maps.newHashMap();
    h1.put("k", "v");
    h1.put("x", "y");
    h1.put("user-agent", "firefox");
    CaseInsensitiveStringKV H1 = new CaseInsensitiveStringKV(h1);

    Map <String, String> h2 = Maps.newHashMap();
    h2.put("x", "y");
    h2.put("user-agent", "firefox");
    CaseInsensitiveStringKV H2 = new CaseInsensitiveStringKV(h2);

    HttpRequest R1 = new HttpRequest(
        HttpRequestMethod.GET,
        H1,
        new RemoteIpAddresses("1.2.3.4", Sets.newHashSet("2.2.2.2", "3.3.3.3")),
        "/"
    );

    HttpRequest R2 = new HttpRequest(
        HttpRequestMethod.POST,
        H2,
        new RemoteIpAddresses("1.2.3.4", Sets.newHashSet("3.3.3.3", "2.2.2.2")),
        "/abc"
    );

    assertThat(R1).isEqualTo(R2);
  }
}
