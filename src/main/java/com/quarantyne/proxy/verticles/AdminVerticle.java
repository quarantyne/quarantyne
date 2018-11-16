package com.quarantyne.proxy.verticles;

import com.quarantyne.config.IpPort;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminVerticle extends AbstractVerticle {
  // todo migrate these to webrouter
  private static final String HEALTH_PATH = "/health";
  private static final String METRICS_PATH = "/metrics";
  private static final String CONTENT_JSON = "application/json; charset=utf-8";

  private final IpPort ipPort;
  private MetricsService metricsService;

  private static final String METRICS_HTTP_PROXY_FQDN = "vertx.http.clients";
  private static final String METRICS_HTTP_REMOTE_FQDN = "vertx.http.servers";

  public AdminVerticle(IpPort ipPort) {
    this.ipPort = ipPort;
  }

  public void start() {
    this.metricsService = MetricsService.create(vertx);

    HttpServer httpServer = vertx.createHttpServer();
    httpServer.requestHandler(req -> {
      if (req.path().equals(HEALTH_PATH)) {
        req.response().end("ok");
      } else if (req.path().equals(METRICS_PATH)) {
        publishMetricsSnapshot(req.response());
      } else {
        req.response().setStatusCode(404).end("HTTP 404");
      }
    });
    // we don't start this verticle unless this was defined
    httpServer.listen(ipPort.getPort(), ipPort.getIp(), h -> {
      if (h.failed()) {
        log.info("failed to start admin service", h.cause());
      }
    });
  }

  private void publishMetricsSnapshot(HttpServerResponse rep) {
    rep.headers().set(HttpHeaders.CONTENT_TYPE, CONTENT_JSON);
    JsonObject o = new JsonObject();
    o.put("http_proxy", metricsService.getMetricsSnapshot(METRICS_HTTP_PROXY_FQDN));
    o.put("http_remote", metricsService.getMetricsSnapshot(METRICS_HTTP_REMOTE_FQDN));
    rep.end(o.toString());
  }
}
