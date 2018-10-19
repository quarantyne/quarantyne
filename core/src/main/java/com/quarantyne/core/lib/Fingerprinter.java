package com.quarantyne.core.lib;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.quarantyne.core.util.CaseInsensitiveStringKV;
import java.util.Set;

public final class Fingerprinter {
  static HashFunction h = Hashing.murmur3_32();

  static Set<String> FINGERPRINTED_HEADERS = Sets.newHashSet(
      "accept",
      "accept-encoding",
      "accept-language",
      "cookie",
      "content-length", // in post/put req
      "content-type",   // in post/put req
      "dnt",
      "upgrade-insecure-requests",
      "user-agent"
  );

  public static HashCode fromString(String str) {
    return h.hashString(str, Charsets.UTF_8);
  }
  public static HashCode fromHeaders(CaseInsensitiveStringKV headers) {
    String values = headers.entrySet().stream().filter(kv ->
      FINGERPRINTED_HEADERS.contains(kv.getKey().toLowerCase())
    ).map(e -> e.getValue().toLowerCase()).sorted().reduce("", (x,y) -> x+y);

    return h.hashString(values, Charsets.UTF_8);
  }
}
