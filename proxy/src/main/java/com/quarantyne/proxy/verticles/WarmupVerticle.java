package com.quarantyne.proxy.verticles;

import com.quarantyne.proxy.ServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WarmupVerticle extends AbstractVerticle {

  private final ServerConfig serverConfig;
  private HttpClient httpClient;

  public WarmupVerticle(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  @Override
  public void start(Future<Void> startFuture) {
    this.httpClient = vertx.createHttpClient();
    Future<Void> warmupFront = warmup(serverConfig.getProxyHost(), serverConfig.getProxyPort());
    Future<Void> warmupBack = warmup(serverConfig.getRemoteHost(), serverConfig.getRemotePort());
    CompositeFuture.join(warmupFront, warmupBack).setHandler(h -> {
      if (h.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(h.cause());
      }
    });
  }

  private Future<Void> warmup(String host, int port) {
    Future<Void> f = Future.future();
    httpClient
        .request(HttpMethod.GET, port, host, "/")
        .exceptionHandler(ex -> {
          log.error("error while warming up to " + host, ex);
        }).handler(r -> {
      if (isSuccess(r.statusCode())) {
        log.info("warmup of {}:{} complete", host, port);
        f.complete();
      } else {
        log.error("warmup to {}:{} failed", host, port);
        f.fail("warmup failed");
      }
    }).end();
    return f;
  }

  private boolean isSuccess(int httpStatusCode) {
    return httpStatusCode >= 200 && httpStatusCode < 300;
  }

  @Override
  public void stop() {
    log.info("shutting down http warmup client");
    httpClient.close();
  }
}
