package com.quarantyne.config;

import com.google.common.base.Strings;
import lombok.Value;
import org.apache.commons.validator.routines.InetAddressValidator;

@Value
public class IpPort {
  String ip;
  int port;

  static IpPort parse(String str) throws IllegalArgumentException {
    if (Strings.isNullOrEmpty(str)) {
      throw new IllegalArgumentException("ip:port null or empty");
    }

    String[] parts = str.split(":");

    if (parts.length == 2 && InetAddressValidator.getInstance().isValidInet4Address(parts[0])) {
      int port;
      try {
        port = Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid ip:port {}" + str);
      }
      if (port > 0 && port < 65536){
        return new IpPort(parts[0], port);
      }
    }
    throw new IllegalArgumentException("Invalid ip:port {}" + str);
  }

  public String toHuman() {
    return String.format("%s:%s", ip, port);
  }
}
