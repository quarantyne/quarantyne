package com.quarantyne.core.lib;

import java.nio.charset.Charset;
import lombok.Value;

@Value
public class ContentType {
  String contentType;
  Charset charset;
}
