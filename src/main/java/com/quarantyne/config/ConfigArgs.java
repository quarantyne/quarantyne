package com.quarantyne.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.net.URL;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;

@Slf4j
public final class ConfigArgs {
  @Parameter(names = "--ingress", description = "ip:port of inbound web traffic.")
  private String ingress = "0.0.0.0:8080";

  @Parameter(names = "--egress", description = "HTTP destination where Quarantyne forwards annotated web traffic.")
  private String egress = "http://httpbin.org";

  @Parameter(names = "--admin", description = "internal ip:port where to access admin, UI and metrics. Optional")
  private String adminIpPort = null;

  @Parameter(names = "--config-file", description = "Optional URL or local path to a Quarantyne JSON configuration file")
  private String configFile = null;

  @Parameter(names = {"--help", "-help", "--h", "-h"}, description = "Display help about available configuration arguments")
  private boolean help = false;

  public static ConfigArgs parse(String...args) {
    ConfigArgs configArgs = new ConfigArgs();
    JCommander jCommander = new JCommander(configArgs);
    jCommander.parse(args);
    if (configArgs.help) {
      jCommander.usage();
      System.exit(0);
    }
    return configArgs;
  }

  public IpPort getIngress() {
    IpPort ipPort = null;
    try {
      ipPort = IpPort.parse(ingress);
    } catch (IllegalArgumentException ex) {
      log.error("Cannot start because ingress ("+ingress+") is not a valid ip:port");
      System.exit(-1);
    }
    return ipPort;
  }

  public EgressUrl getEgress() {
    EgressUrl egressUrl = null;
    try {
      if (!UrlValidator.getInstance().isValid(egress) || egress.startsWith("https")) {
        throw new Exception();
      }
      URL url = new URL(egress);
      int port = 80;
      if (url.getPort() != -1) {
        port = url.getPort();
      }
      egressUrl = new EgressUrl(url.getProtocol(), url.getHost(), port);
    } catch (Exception e) {
      log.error("Cannot start because egress ("+egress+") is not a valid HTTP URL. HTTPS is not supported");
      System.exit(-1);
    }
    return egressUrl;
  }

  public Optional<IpPort> getAdminIpPort() {
    Optional<IpPort> ipPort = null;
    if (adminIpPort != null) {
      try {
        ipPort = Optional.of(IpPort.parse(adminIpPort));
      } catch (IllegalArgumentException ex) {
        log.error("Cannot start because adminIpPort ("+ingress+") is not a valid ip:port");
        System.exit(-1);
      }
      return ipPort;
    } else {
      return Optional.empty();
    }
  }

  public Optional<String> getConfigFile() {
    return Optional.ofNullable(configFile);
  }


  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ConfigArgs{");
    sb.append("ingress='").append(ingress).append('\'');
    sb.append(", egress='").append(egress).append('\'');
    sb.append(", adminIpPort='").append(adminIpPort).append('\'');
    sb.append(", configFile='").append(configFile).append('\'');
    sb.append('}');
    return sb.toString();
  }
}