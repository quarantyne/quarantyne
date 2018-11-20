package com.quarantyne.lib;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Set;

public class RemoteIpAddressesParser {
  //TODO Forwarded: by=<identifier>; for=<identifier>; host=<host>; proto=<http|https>
  //TODO X-Forwarded-Host: <host>

  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  private static final String COMMA = ",";
  private static final Set<String> EMPTY = Sets.newHashSet();

  //TODO validate IP format quad[,quad...]
  public static RemoteIpAddresses parse(
      CaseInsensitiveStringKV requestHeaders,
      String originIp) {
    String xForwardedFor = requestHeaders.get(X_FORWARDED_FOR);
    if (! Strings.isNullOrEmpty(xForwardedFor)) {
      Set<String> proxyIps = Sets.newHashSet();
      String[] tokens = xForwardedFor.split(COMMA);
      // first ip is client, next is proxies (note that this is untrusted)
      String origin = null;
      for(int i = 0; i < tokens.length; i++) {
        if (i == 0) {
          origin = tokens[i].trim();
        } else {
          proxyIps.add(tokens[i].trim());
        }
      }
      proxyIps.add(originIp);
      return new RemoteIpAddresses(origin, proxyIps);
    } else {
      return new RemoteIpAddresses(originIp, EMPTY);
    }

  }
}
