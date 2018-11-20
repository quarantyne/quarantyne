package com.quarantyne.lib;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Value;

@Value
public class RemoteIpAddresses {
  private static Set<String> EMPTY = ImmutableSet.of();
  String origin;
  Set<String> proxies;
  Set<String> all;

  public RemoteIpAddresses(String origin) {
    this(origin, EMPTY);
  }

  RemoteIpAddresses(String origin, Set<String> proxies) {
    this.origin = origin;
    this.proxies = ImmutableSet.copyOf(proxies);

    Set<String> all = Sets.newHashSet(origin);
    all.addAll(proxies);
    this.all = ImmutableSet.copyOf(all);
  }
}
