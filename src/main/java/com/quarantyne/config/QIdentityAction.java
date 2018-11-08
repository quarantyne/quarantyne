package com.quarantyne.config;

import com.google.common.base.Strings;
import lombok.Value;

@Value
public class QIdentityAction {
  String path;
  String identifierParam;
  String secretParam;

  public boolean isEnabledForPath(String path) {
    return isValid() && matchesPath(path);
  }

  public boolean matchesPath(String path) {
    if (Strings.isNullOrEmpty(path)) {
      return false;
    }
    return this.path.equals(path);
  }


  private boolean isValid() {
    return !Strings.isNullOrEmpty(path)
        && !Strings.isNullOrEmpty(identifierParam)
        && !Strings.isNullOrEmpty(secretParam);
  }
}
