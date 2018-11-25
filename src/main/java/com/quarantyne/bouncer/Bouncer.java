package com.quarantyne.bouncer;

import com.quarantyne.config.Config;
import com.quarantyne.config.ConfigArgs;

import com.quarantyne.config.EgressUrl;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bouncer {

  private HttpClient httpClient;
  private Supplier<Config> configSupplier;
  private ConfigArgs configArgs;
  private EgressUrl egressUrl;
  private String path;

  public Bouncer(Vertx vertx, Supplier<Config> configSupplier, ConfigArgs configArgs) {

    this.configSupplier = configSupplier;
    this.configArgs = configArgs;

    String blockedPage = configSupplier.get().getBlockedRequestPage();
    egressUrl = configArgs.getEgress();
    path = null;
    if (!blockedPage.startsWith("/") && blockedPage.startsWith("http")) {
      try {
        URL url = new URL(blockedPage);
        int port = url.getPort();
        if (url.getPort() < 0 && url.getProtocol().equals("https")) {
          port = 443;
        }
        if (url.getPort() < 0 && url.getProtocol().equals("http")) {
          port = 80;
        }
        egressUrl = new EgressUrl(url.getProtocol(), url.getHost(), port);
        path = url.getPath();
      } catch (MalformedURLException ex) {
        path = "/";
        log.error("cannot parse bounced page URL, defaulting to remote /");
      }
    } else {
      path = blockedPage;
    }
    log.info("bouncing to {} path {}", egressUrl.toHuman(), path);

    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setKeepAlive(true);
    if (egressUrl.isSsl()) {
      httpClientOptions.setSsl(true);
    }

    this.httpClient = vertx.createHttpClient(httpClientOptions);
  }

  public void bounce(HttpServerResponse frontRep) {

    HttpClientRequest bouncerReq = httpClient.get(egressUrl.getPort(), egressUrl.getHost(), path);
    bouncerReq.headers().set(HttpHeaders.HOST, egressUrl.getHost());
    bouncerReq.handler(bouncerRep -> {
      Buffer body = Buffer.buffer();
      bouncerRep.handler(body::appendBuffer);
      bouncerRep.endHandler(h -> {
        log.info("bouncing request");
        frontRep.setStatusCode(200).end(body);
      });
    }).exceptionHandler(ex -> {
      log.error("error while bouncing request", ex);
      frontRep.setStatusCode(500).end("Internal Server Error");
    }).end();
  }
}
