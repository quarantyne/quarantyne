package com.quarantyne.lib;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Objects;
import lombok.Value;


@Value
public class HttpRequest {
  HttpRequestMethod method;
  CaseInsensitiveStringKV headers;
  RemoteIpAddresses remoteIpAddresses;
  String path;
  String fingerprint;

  public HttpRequest(HttpRequestMethod method, CaseInsensitiveStringKV headers,
      RemoteIpAddresses remoteIpAddresses, String path) {
    this.method = method;
    this.headers = headers;
    this.remoteIpAddresses = remoteIpAddresses;
    this.path = path;

    this.fingerprint = Hashing.murmur3_128().newHasher()
        .putString(headers.getOrDefault("user-agent", ""), Charsets.UTF_8)
        .putInt(remoteIpAddresses.hashCode())
        .hash().toString();
  }
  public String getIdentity() {
    return headers.getOrDefault("user-agent", "n/a")+"@"+remoteIpAddresses.getAll();
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpRequest that = (HttpRequest) o;
    return getFingerprint().equals(that.getFingerprint());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFingerprint());
  }
}
