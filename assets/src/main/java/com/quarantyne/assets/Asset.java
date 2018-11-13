package com.quarantyne.assets;


import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Asset {
  public abstract Void write();
  Path to(String fileName) {
      return Paths.get("assets","src", "main", "resources", "com", "quarantyne", "assets", fileName);
  }
}
