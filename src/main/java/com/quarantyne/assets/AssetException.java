package com.quarantyne.assets;

public class AssetException extends Exception {

  public AssetException(Throwable cause) {
    super(cause);
  }

  public AssetException(String message, Throwable cause) {
    super(message, cause);
  }
}
