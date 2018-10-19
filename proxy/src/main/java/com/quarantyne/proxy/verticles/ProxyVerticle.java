package com.quarantyne.proxy.verticles;

import com.google.common.base.Joiner;
import com.quarantyne.core.classifiers.CompositeClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestMethod;
import com.quarantyne.core.util.CaseInsensitiveStringKV;
import com.quarantyne.proxy.QuarantyneHeaders;
import com.quarantyne.proxy.ServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProxyVerticle extends AbstractVerticle {

  private final ServerConfig serverConfig;
  private final CompositeClassifier quarantyneClassifier;

  public ProxyVerticle(ServerConfig serverConfig, CompositeClassifier quarantyneClassifier) {
    this.serverConfig = serverConfig;
    this.quarantyneClassifier = quarantyneClassifier;
  }

  @Override
  public void start(Future<Void> startFuture) {
    // proxy server (this server)
    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setHost(serverConfig.getProxyHost());
    httpServerOptions.setUsePooledBuffers(true);
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

    // http client to remote
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setKeepAlive(true);
    httpClientOptions.setLogActivity(false);

    if (serverConfig.getSsl() | serverConfig.getRemotePort() == 443) {
      httpClientOptions.setSsl(true);
    }
    httpClientOptions.setDefaultHost(serverConfig.getRemoteHost());
    httpClientOptions.setDefaultPort(serverConfig.getRemotePort());

    HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

    httpServer.requestHandler(frontReq -> {
      frontReq.bodyHandler(reqBody -> {
        HttpServerResponse frontRep = frontReq.response();
        HttpClientRequest backReq = httpClient.request(
            frontReq.method(),
            frontReq.uri()
        );
        backReq.headers().setAll(frontReq.headers());
        backReq.headers().set(HttpHeaders.HOST, serverConfig.getRemoteHost());
        // inject quarantyne headers, if any
        backReq.headers().setAll(quarantyneCheck(frontReq));
        // --------------------------------
        backReq.handler(backRep -> {
          backRep.bodyHandler(repBody -> {
            frontRep.setStatusCode(backRep.statusCode());
            frontRep.headers().setAll(backRep.headers());
            frontRep.end(repBody);
          });
        });
        backReq.exceptionHandler(ex -> {
          log.error("error while querying downstream service", ex);
          frontRep.setStatusCode(500);
          frontRep.end("Internal Server Error. This request cannot be satisfied.");
        });

        backReq.end(reqBody);
      });
    });

    httpServer.exceptionHandler(ex -> {
      log.error("HTTP server error", ex);
    });

    httpServer.listen(serverConfig.getProxyPort(), serverConfig.getProxyHost(), h -> {
      if (h.failed()) {
        log.error("proxy failed to start", h.cause());
        startFuture.fail(h.cause());
      }
    });
  }

  private Joiner joiner = Joiner.on(",");

  // returns quarantyne headers
  private MultiMap quarantyneCheck(HttpServerRequest req) {
    HttpRequest httpRequest = new HttpRequest(
        HttpRequestMethod.valueOf(req.method().toString().toUpperCase()),
        new CaseInsensitiveStringKV(req.headers().entries()),
        req.remoteAddress().host(),
        req.path()
    );
    MultiMap quarantyneHeaders = MultiMap.caseInsensitiveMultiMap();
    Set<Label> quarantyneLabels = quarantyneClassifier.classify(httpRequest);
    if (!quarantyneLabels.isEmpty()) {
      quarantyneHeaders.add(QuarantyneHeaders.LABELS, joiner.join(quarantyneLabels));
      quarantyneHeaders.add(QuarantyneHeaders.TRACE_ID, UUID.randomUUID().toString());
    }
    return quarantyneHeaders;
  }
}
