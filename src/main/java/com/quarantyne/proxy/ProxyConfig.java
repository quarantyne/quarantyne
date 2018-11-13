package com.quarantyne.proxy;

import com.beust.jcommander.Parameter;

public final class ProxyConfig {
  @Parameter(names = "--proxyPort", description = "proxy port")
  private Integer proxyPort = 8080;

  @Parameter(names = "--proxyHost", description = "proxy host")
  private String proxyHost = "127.0.0.1";

  @Parameter(names = "--remotePort", description = "remote port")
  private Integer remotePort = 80;

  @Parameter(names = "--remoteHost", description = "remote host")
  private String remoteHost = "httpbin.org";

  @Parameter(names = "--remoteSsl", description = "remote host is over SSL")
  private Boolean isSsl = false;

  @Parameter(names = "--adminPort", description = "internal port from which health and metrics are published")
  private Integer adminPort = 3231;

  @Parameter(names = "--config-url", description = "URL to Quarantyne JSON configuration file eg. https://files.example.com/quarantyne.json")
  private String configUrl = null;

  @Parameter(names = "--config-file", description = "Absolute local path to Quarantyne JSON configuration file eg. /etc/quarantyne.json")
  private String configFile = null;

  public Integer getProxyPort() {
    return proxyPort;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public Integer getRemotePort() {
    return remotePort;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public Boolean getSsl() {
    return isSsl;
  }

  public Integer getAdminPort() {
    return adminPort;
  }

  public String getConfigUrl() {
    return configUrl;
  }

  public String getConfigFile() {
    return configFile;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ProxyConfig{");
    sb.append("proxyPort=").append(proxyPort);
    sb.append(", proxyHost='").append(proxyHost).append('\'');
    sb.append(", remotePort=").append(remotePort);
    sb.append(", remoteHost='").append(remoteHost).append('\'');
    sb.append(", isSsl=").append(isSsl);
    sb.append(", adminPort=").append(adminPort);
    sb.append(", configUrl='").append(configUrl).append('\'');
    sb.append(", configFile='").append(configFile).append('\'');
    sb.append('}');
    return sb.toString();
  }
}