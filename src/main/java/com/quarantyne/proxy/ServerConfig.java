package com.quarantyne.proxy;

import com.beust.jcommander.Parameter;

public final class ServerConfig {
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
}