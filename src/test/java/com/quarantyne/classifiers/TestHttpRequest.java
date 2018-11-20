package com.quarantyne.classifiers;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestMethod;
import com.quarantyne.lib.RemoteIpAddresses;
import com.quarantyne.util.CaseInsensitiveStringKV;
import java.util.Map;

public class TestHttpRequest {

  public static String DEFAULT_USER_AGENT =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:62.0) Gecko/20100101 Firefox/62.0";

  public static CaseInsensitiveStringKV DEFAULT_HEADERS = defaultHeaders();

  private static CaseInsensitiveStringKV defaultHeaders() {
    Map<String, String> h = Maps.newHashMap();
    h.put(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    h.put(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
    h.put(HttpHeaders.CONNECTION, "Close");
    h.put(HttpHeaders.COOKIE, "theme=light; sessionToken=abc123");
    h.put(HttpHeaders.DNT, "1");
    h.put(HttpHeaders.HOST, "www.example.org");
    h.put(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);
    return new CaseInsensitiveStringKV(h);
  }

  public static HttpRequest REQ() { return new Builder().build(); }

  public static class Builder {
    HttpRequestMethod method = HttpRequestMethod.GET;
    CaseInsensitiveStringKV headers = defaultHeaders();
    String remoteAddress = "1.2.3.4";
    String path = "/";

    public Builder() { }
    public Builder setMethod(HttpRequestMethod method) {
      this.method = method;
      return this;
    }
    public Builder setHeaders(CaseInsensitiveStringKV headers) {
      this.headers = headers;
      return this;
    }
    public Builder setRemoteAddress(String remoteAddress) {
      this.remoteAddress = remoteAddress;
      return this;
    }
    public Builder setPath(String path) {
      this.path = path;
      return this;
    }
    public HttpRequest build() {
      return new HttpRequest(method, headers, new RemoteIpAddresses(remoteAddress), path);
    }

  }
}
