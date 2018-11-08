package com.quarantyne.proxy.verticles;

import com.google.common.base.Joiner;
import com.quarantyne.config.Config;
import com.quarantyne.core.classifiers.CompositeClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.lib.HttpRequestBodyParser;
import com.quarantyne.core.lib.HttpRequestMethod;
import com.quarantyne.core.lib.HttpResponse;
import com.quarantyne.core.util.CaseInsensitiveStringKV;
import com.quarantyne.proxy.ProxyConfig;
import com.quarantyne.proxy.QuarantyneHeaders;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProxyVerticle extends AbstractVerticle {
  private final ProxyConfig proxyConfig;
  private final CompositeClassifier quarantyneClassifier;
  private HttpClient httpClient;
  private Supplier<Config> configSupplier;

  public ProxyVerticle(ProxyConfig proxyConfig,
      CompositeClassifier quarantyneClassifier,
      Supplier<Config> configSupplier) {
    this.proxyConfig = proxyConfig;
    this.quarantyneClassifier = quarantyneClassifier;
    this.configSupplier = configSupplier;
  }

  @Override
  public void start(Future<Void> startFuture) {
    // proxy server (this server)
    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setHost(proxyConfig.getProxyHost());
    httpServerOptions.setUsePooledBuffers(true);
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

    // http client to remote
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setKeepAlive(true);
    httpClientOptions.setLogActivity(true);

    if (proxyConfig.getSsl() | proxyConfig.getRemotePort() == 443) {
      httpClientOptions.setSsl(true);
    }
    httpClientOptions.setDefaultHost(proxyConfig.getRemoteHost());
    httpClientOptions.setDefaultPort(proxyConfig.getRemotePort());

    this.httpClient = vertx.createHttpClient(httpClientOptions);

    httpServer.requestHandler(frontReq -> {
      if (frontReq.method().equals(HttpMethod.POST) || frontReq.method().equals(HttpMethod.PUT)) {
        frontReq.bodyHandler(reqBody -> {
          proxiedRequestHandler(frontReq, reqBody);
        });
      } else {
        proxiedRequestHandler(frontReq, null);
      }
    }).exceptionHandler(ex -> {
      log.error("HTTP server error", ex);
    }).listen(proxyConfig.getProxyPort(), proxyConfig.getProxyHost(), h -> {
      if (h.failed()) {
        log.error("proxy failed to start", h.cause());
        startFuture.fail(h.cause());
      }
    });
  }

  private void proxiedRequestHandler(HttpServerRequest frontReq, @Nullable Buffer frontReqBody) {
    HttpServerResponse frontRep = frontReq.response();
    HttpClientRequest backReq = httpClient.request(
        frontReq.method(),
        frontReq.uri()
    );

    backReq.headers().setAll(frontReq.headers());
    backReq.headers().set(HttpHeaders.HOST, proxyConfig.getRemoteHost());
    // inject quarantyne headers, if any
    HttpRequest qReq = new HttpRequest(
        HttpRequestMethod.valueOf(frontReq.method().toString().toUpperCase()),
        new CaseInsensitiveStringKV(frontReq.headers().entries()),
        frontReq.remoteAddress().host(),
        frontReq.path()
    );
    @Nullable final HttpRequestBody qBody =
        getBody(qReq.getMethod(), frontReqBody, frontReq.getHeader(HttpHeaders.CONTENT_TYPE));

    backReq.headers().addAll(quarantyneCheck(qReq, qBody));
    // --------------------------------
    backReq.handler(backRep -> {
      Buffer body = Buffer.buffer();
      backRep.handler(body::appendBuffer);
      backRep.endHandler(h -> {
        // callback quarantyne with data to record, if needed
        quarantyneClassifier.record(new HttpResponse(backRep.statusCode()), qReq, qBody);
        // --------------------------------
        frontRep.setStatusCode(backRep.statusCode());
        frontRep.headers().setAll(backRep.headers());
        frontRep.end(body);
      });
    });
    backReq.exceptionHandler(ex -> {
      log.error("error while querying downstream service", ex);
      frontRep.setStatusCode(500);
      frontRep.end("Internal Server Error. This request cannot be satisfied.");
    });
    if (frontReqBody != null) {
      backReq.end(frontReqBody);
    } else {
      backReq.end();
    }
  }

  private Joiner joiner = Joiner.on(",");

  // returns quarantyne headers

  private MultiMap quarantyneCheck(HttpRequest req, HttpRequestBody body) {
    Set<Label> quarantyneLabels = quarantyneClassifier.classify(req, body);

    MultiMap quarantyneHeaders = MultiMap.caseInsensitiveMultiMap();
    if (!quarantyneLabels.isEmpty()) {
      quarantyneHeaders.add(QuarantyneHeaders.LABELS, joiner.join(quarantyneLabels));
      quarantyneHeaders.add(QuarantyneHeaders.TRACE_ID, UUID.randomUUID().toString());
    }
    return quarantyneHeaders;
  }

  @Nullable
  private HttpRequestBody getBody(
      HttpRequestMethod method,
      @Nullable Buffer frontReqBody,
      @Nullable String contentType) {
    if (frontReqBody != null && contentType != null &&
        (method.equals(HttpRequestMethod.POST)
            || method.equals(HttpRequestMethod.PUT)
            || method.equals(HttpRequestMethod.PATCH))) {
      return HttpRequestBodyParser.parse(frontReqBody.getBytes(), contentType);
    } else {
      return null;
    }
  }
}
