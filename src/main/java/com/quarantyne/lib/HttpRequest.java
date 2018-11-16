package com.quarantyne.lib;

import com.quarantyne.util.CaseInsensitiveStringKV;
import lombok.Value;


@Value
public class HttpRequest {
  HttpRequestMethod method;
  CaseInsensitiveStringKV headers;
  String remoteAddress;
  String path;
  public String getId() {
    return remoteAddress+":"+headers.getOrDefault("user-agent", "");
  }
}
