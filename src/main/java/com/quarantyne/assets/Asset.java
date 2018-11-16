package com.quarantyne.assets;

import com.google.common.io.Resources;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Asset {
  private final byte[] bytes;
  private static String PREFIX = "com/quarantyne/assets/";

  Asset(String name) throws AssetException {
    try {
      this.bytes = Resources.toByteArray(Resources.getResource(prefixed(name)));
    } catch (IOException ex) {
      throw new AssetException("Asset " + prefixed(name) + " cannot be read or found", ex);
    }
  }

  private static String prefixed(String name) {
    return PREFIX + name;
  }

  public BufferedInputStream getBytes() {
    return new BufferedInputStream(new ByteArrayInputStream(bytes));
  }
}
