package com.quarantyne.proxy;

import com.beust.jcommander.JCommander;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.quarantyne.core.bloom.BloomFilters;
import com.quarantyne.core.classifiers.CompositeClassifier;
import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.HttpRequestWithBodyClassifier;
import com.quarantyne.core.classifiers.impl.CompromisedPasswordClassifier;
import com.quarantyne.core.classifiers.impl.DisposableEmailClassifier;
import com.quarantyne.core.classifiers.impl.FastAgentClassifier;
import com.quarantyne.core.classifiers.impl.IpRotationClassifier;
import com.quarantyne.core.classifiers.impl.LargeBodySizeClassifier;
import com.quarantyne.core.classifiers.impl.SuspiciousRequestHeadersClassifier;
import com.quarantyne.core.classifiers.impl.SuspiciousUserAgentClassifier;
import com.quarantyne.proxy.verticles.AdminVerticle;
import com.quarantyne.proxy.verticles.ProxyVerticle;
import com.quarantyne.proxy.verticles.WarmupVerticle;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  private static BloomFilter<String> weakOrBreachedPwBf = null;
  private static BloomFilter<String> disposableMxBf = null;

  public static void main(String...args) {
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

    ServerConfig serverConfig = new ServerConfig();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(serverConfig)
        .build();

    // jCommander.usage();
    jCommander.parse(args);

    try {
      weakOrBreachedPwBf = BloomFilters.deserialize(BloomFilters.PASSWORDS_BF_RESOURCE);
      disposableMxBf = BloomFilters.deserialize(BloomFilters.MX_DOMAINS_BF_RESOURCE);
    } catch (IOException ioex) {
      log.error("error during bf deserialization", ioex);
      System.exit(-1);
    }

    // quarantyne classifiers
    List<HttpRequestClassifier> httpRequestClassifierList = Lists.newArrayList(
        new FastAgentClassifier(),
        new IpRotationClassifier(),
        new SuspiciousRequestHeadersClassifier(),
        new SuspiciousUserAgentClassifier()
    );

    List<HttpRequestWithBodyClassifier> httpRequestWithBodyClassifierList = Lists.newArrayList(
        new LargeBodySizeClassifier(),
        new CompromisedPasswordClassifier(weakOrBreachedPwBf),
        new DisposableEmailClassifier(disposableMxBf)
    );

    CompositeClassifier quarantyneRequestClassifier = new CompositeClassifier(
        httpRequestClassifierList,
        httpRequestWithBodyClassifierList
    );

    log.info("==> quarantyne");
    log.info("==> proxy   @ {}:{}", serverConfig.getProxyHost(), serverConfig.getProxyPort());
    log.info("==> remote  @ {}:{}", serverConfig.getRemoteHost(), serverConfig.getRemotePort());
    log.info("==> admin   @ http://{}:{}", serverConfig.getProxyHost(), serverConfig.getAdminPort());

    int numCpus = CpuCoreSensor.availableProcessors();

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setPreferNativeTransport(true);
    vertxOptions.setMetricsOptions(
        new DropwizardMetricsOptions().setEnabled(true)
    );

    log.debug("==> event loop size is {}", vertxOptions.getEventLoopPoolSize());
    log.debug("==> detected {} cpus core", numCpus);
    Vertx vertx = Vertx.vertx(vertxOptions);

    vertx.deployVerticle(new AdminVerticle(serverConfig));

    vertx.deployVerticle(() -> new ProxyVerticle(serverConfig, quarantyneRequestClassifier),
        new DeploymentOptions().setInstances(numCpus * 2),
        proxyVerticle -> {
          vertx.deployVerticle(() -> new WarmupVerticle(serverConfig),
              new DeploymentOptions(),
              warmupVerticle -> vertx.undeploy(warmupVerticle.result()));
        });
  }

}
