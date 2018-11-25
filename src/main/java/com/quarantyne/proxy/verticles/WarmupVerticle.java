package com.quarantyne.proxy.verticles;

import com.quarantyne.config.ConfigArgs;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WarmupVerticle extends AbstractVerticle {

  private final ConfigArgs configArgs;
  private HttpClient httpClient;

  public WarmupVerticle(ConfigArgs configArgs) {
    this.configArgs = configArgs;
  }

  @Override
  public void start(Future<Void> startFuture) {
    this.httpClient = vertx.createHttpClient();
    Future<Void> warmupFront = warmup(configArgs.getIngress().getIp(), configArgs.getIngress().getPort());
    Future<Void> warmupBack = warmup(configArgs.getEgress().getHost(), configArgs.getEgress().getPort());
    vertx.setPeriodic(1000, h -> {
      CompositeFuture.join(warmupFront, warmupBack).setHandler(timer -> {
        if (timer.succeeded()) {
          startFuture.complete();
        }
      });
    });

  }

  private Future<Void> warmup(String host, int port) {
    Future<Void> f = Future.future();
    httpClient
        .request(HttpMethod.GET, port, host, "/")
        .exceptionHandler(ex -> {
          log.error("error while warming up to {}, reason: {}. Retrying...", host, ex.getMessage());
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
    log.debug("shutting down http warmup client");
    log.info("==> ready");
    httpClient.close();
  }
}
