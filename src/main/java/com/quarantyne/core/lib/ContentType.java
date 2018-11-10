package com.quarantyne.core.lib;

import java.nio.charset.Charset;
import javax.annotation.Nullable;
import lombok.Value;

@Value
public class ContentType {
  String contentType;
  Charset charset;
}
