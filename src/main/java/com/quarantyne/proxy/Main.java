package com.quarantyne.proxy;

import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.quarantyne.assets.AssetException;
import com.quarantyne.assets.AssetRegistry;
import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.MainClassifier;
import com.quarantyne.classifiers.impl.CompromisedPasswordClassifier;
import com.quarantyne.classifiers.impl.DisposableEmailClassifier;
import com.quarantyne.classifiers.impl.FastAgentClassifier;
import com.quarantyne.classifiers.impl.GeoDiscrepancyClassifier;
import com.quarantyne.classifiers.impl.IpRotationClassifier;
import com.quarantyne.classifiers.impl.LargeBodySizeClassifier;
import com.quarantyne.classifiers.impl.PublicCloudExecutionClassifier;
import com.quarantyne.classifiers.impl.SuspiciousRequestHeadersClassifier;
import com.quarantyne.classifiers.impl.SuspiciousUserAgentClassifier;
import com.quarantyne.config.ConfigArgs;
import com.quarantyne.config.ConfigRetrieverOptionsSupplier;
import com.quarantyne.config.ConfigSupplier;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoIp4jImpl;
import com.quarantyne.proxy.verticles.AdminVerticle;
import com.quarantyne.proxy.verticles.ProxyVerticle;
import com.quarantyne.proxy.verticles.WarmupVerticle;
import com.quarantyne.util.BloomFilters;
import com.quarantyne.util.CidrMembership;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  private static BloomFilter<String> weakOrBreachedPwBf = null;
  private static BloomFilter<String> disposableMxBf = null;
  private static CidrMembership<String> awsIpMembership = null;
  private static CidrMembership<String> gcpIpMembership = null;

  public static void main(String...args) {
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

    ConfigArgs configArgs = ConfigArgs.parse(args);

    // load assets or die
    try {
      weakOrBreachedPwBf = BloomFilters.deserialize(AssetRegistry.getCompromisedPasswords());
      disposableMxBf = BloomFilters.deserialize(AssetRegistry.getDisposableEmails());
      awsIpMembership = new CidrMembership<>(AssetRegistry.getAwsIps(), "aws");
      gcpIpMembership = new CidrMembership<>(AssetRegistry.getGcpIps(), "gcp");
    } catch (AssetException ex) {
      log.error("error while reading asset", ex);
      System.exit(-1);
    }

    final GeoIp4j geoIp4j = new GeoIp4jImpl();

    log.info("{} <= quarantyne => {}", configArgs.getIngress().toHuman(), configArgs.getEgress().toHuman());

    configArgs.getAdminIpPort().ifPresent(ipPort -> {
      log.info("==> admin @ http://{}:{}", ipPort.getIp(), ipPort.getPort());
    });

    log.info("see available options with --help");
    int numCpus = CpuCoreSensor.availableProcessors();

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setPreferNativeTransport(true);
    vertxOptions.setMetricsOptions(
        new DropwizardMetricsOptions().setEnabled(true)
    );

    log.debug("==> event loop size is {}", vertxOptions.getEventLoopPoolSize());
    log.debug("==> detected {} cpus core", numCpus);
    Vertx vertx = Vertx.vertx(vertxOptions);

    ConfigSupplier configSupplier;
    if (configArgs.getConfigFile().isPresent()) {
      configSupplier = new ConfigSupplier(vertx,
          new ConfigRetrieverOptionsSupplier(configArgs.getConfigFile().get()));
    } else {
      log.info("No configuration file was specified, using default settings");
      configSupplier = new ConfigSupplier();
    }

    // quarantyne classifiers
    List<HttpRequestClassifier> httpRequestClassifierList = Lists.newArrayList(
        new FastAgentClassifier(),
        new IpRotationClassifier(),
        new SuspiciousRequestHeadersClassifier(),
        new SuspiciousUserAgentClassifier(),
        new LargeBodySizeClassifier(),
        new CompromisedPasswordClassifier(weakOrBreachedPwBf, configSupplier),
        new DisposableEmailClassifier(disposableMxBf, configSupplier),
        new GeoDiscrepancyClassifier(geoIp4j, configSupplier),
        new PublicCloudExecutionClassifier(awsIpMembership, gcpIpMembership)
        // new SuspiciousLoginActivityClassifier(geoIp4j)
    );

    MainClassifier mainClassifier = new MainClassifier(httpRequestClassifierList);

    if (configArgs.getAdminIpPort().isPresent()) {
      vertx.deployVerticle(new AdminVerticle(configArgs.getAdminIpPort().get()));
    }

    vertx.deployVerticle(() -> new ProxyVerticle(configArgs, mainClassifier,
            configSupplier),
        new DeploymentOptions().setInstances(numCpus * 2 + 1));

    vertx.deployVerticle(() -> new WarmupVerticle(configArgs),
        new DeploymentOptions(),
        warmupVerticle -> {
          vertx.undeploy(warmupVerticle.result());
        });

    vertx.exceptionHandler(ex -> {
      log.error("uncaught exception", ex);
    });
  }

}
