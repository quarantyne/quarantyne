package com.quarantyne.core.lib;

import com.quarantyne.core.util.CaseInsensitiveStringKV;
import lombok.Value;


@Value
public class HttpRequest {
  HttpRequestMethod method;
  CaseInsensitiveStringKV headers;
  String remoteAddress;
  String path;
}
