package com.quarantyne.proxy.verticles;

import com.google.common.base.Joiner;
import com.quarantyne.classifiers.MainClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.config.Config;
import com.quarantyne.config.ConfigArgs;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpRequestBodyParser;
import com.quarantyne.lib.HttpRequestMethod;
import com.quarantyne.lib.HttpResponse;
import com.quarantyne.lib.QuarantyneHeaders;
import com.quarantyne.lib.RemoteIpAddressesParser;
import com.quarantyne.util.CaseInsensitiveStringKV;
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
  private final ConfigArgs configArgs;
  private final MainClassifier quarantyneClassifier;
  private HttpClient httpClient;
  private Supplier<Config> configSupplier;

  public ProxyVerticle(ConfigArgs configArgs,
      MainClassifier quarantyneClassifier,
      Supplier<Config> configSupplier) {
    this.configArgs = configArgs;
    this.quarantyneClassifier = quarantyneClassifier;
    this.configSupplier = configSupplier;
  }

  @Override
  public void start(Future<Void> startFuture) {
    // proxy server (this server)
    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setHost(configArgs.getIngress().getIp());
    httpServerOptions.setUsePooledBuffers(true);
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

    // http client to remote
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setKeepAlive(true);
    httpClientOptions.setLogActivity(true);

    if (configArgs.getEgress().isSsl()) {
      httpClientOptions.setSsl(true);
    }
    httpClientOptions.setDefaultHost(configArgs.getEgress().getHost());
    httpClientOptions.setDefaultPort(configArgs.getEgress().getPort());

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
    }).listen(configArgs.getIngress().getPort(), configArgs.getIngress().getIp(), h -> {
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

    CaseInsensitiveStringKV frontReqHeaders =
        new CaseInsensitiveStringKV(frontReq.headers().entries());

    backReq.headers().setAll(frontReq.headers());
    backReq.headers().set(HttpHeaders.HOST, configArgs.getEgress().getHost());
    // inject quarantyne headers, if any
    HttpRequest qReq = new HttpRequest(
        HttpRequestMethod.valueOf(frontReq.method().toString().toUpperCase()),
        frontReqHeaders,
        RemoteIpAddressesParser.parse(frontReqHeaders, frontReq.remoteAddress().host()),
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
    }
    quarantyneHeaders.add(QuarantyneHeaders.TRACE_ID, UUID.randomUUID().toString());
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
