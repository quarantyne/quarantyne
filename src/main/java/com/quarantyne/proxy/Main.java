package com.quarantyne.proxy;

import com.beust.jcommander.JCommander;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.quarantyne.config.ConfigReader;
import com.quarantyne.core.classifiers.CompositeClassifier;
import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.impl.CompromisedPasswordClassifier;
import com.quarantyne.core.classifiers.impl.DisposableEmailClassifier;
import com.quarantyne.core.classifiers.impl.FastAgentClassifier;
import com.quarantyne.core.classifiers.impl.GeoDiscrepancyClassifier;
import com.quarantyne.core.classifiers.impl.IpRotationClassifier;
import com.quarantyne.core.classifiers.impl.LargeBodySizeClassifier;
import com.quarantyne.core.classifiers.impl.PublicCloudExecutionClassifier;
import com.quarantyne.core.classifiers.impl.SuspiciousRequestHeadersClassifier;
import com.quarantyne.core.classifiers.impl.SuspiciousUserAgentClassifier;
import com.quarantyne.core.util.BloomFilters;
import com.quarantyne.core.util.CidrMembership;
import com.quarantyne.geoip4j.GeoIp4j;
import com.quarantyne.geoip4j.GeoIp4jImpl;
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
  private static CidrMembership<String> awsIpMembership = null;
  private static CidrMembership<String> gcpIpMembership = null;

  public static void main(String...args) {
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

    ProxyConfig proxyConfig = new ProxyConfig();
    JCommander.newBuilder()
        .addObject(proxyConfig)
        .build()
        .parse(args);

    // load assets or die
    try {
      weakOrBreachedPwBf = BloomFilters.deserialize("com/quarantyne/assets/compromised_passwords.dat");
      disposableMxBf = BloomFilters.deserialize("com/quarantyne/assets/disposable_email.dat");
      awsIpMembership = new CidrMembership<>("com/quarantyne/assets/aws_ip_ranges.dat", "aws");
      gcpIpMembership = new CidrMembership<>("com/quarantyne/assets/gcp_ip_ranges.dat", "gcp");
    } catch (IOException ioex) {
      log.error("error while reading asset", ioex);
      System.exit(-1);
    }

    final GeoIp4j geoIp4j = new GeoIp4jImpl();

    log.info("==> quarantyne");
    log.info("==> proxy   @ {}:{}", proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
    log.info("==> remote  @ {}:{}", proxyConfig.getRemoteHost(), proxyConfig.getRemotePort());
    log.info("==> admin   @ http://{}:{}", proxyConfig.getProxyHost(), proxyConfig.getAdminPort());

    int numCpus = CpuCoreSensor.availableProcessors();

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setPreferNativeTransport(true);
    vertxOptions.setMetricsOptions(
        new DropwizardMetricsOptions().setEnabled(true)
    );

    log.debug("==> event loop size is {}", vertxOptions.getEventLoopPoolSize());
    log.debug("==> detected {} cpus core", numCpus);
    Vertx vertx = Vertx.vertx(vertxOptions);

    ConfigReader configReader = new ConfigReader(vertx, proxyConfig);
    // require a config to start
    if (configReader.get() == null) {
      log.info("No quarantyne configuration was specified, using default settings");
    }

    // quarantyne classifiers
    List<HttpRequestClassifier> httpRequestClassifierList = Lists.newArrayList(
        new FastAgentClassifier(),
        new IpRotationClassifier(),
        new SuspiciousRequestHeadersClassifier(),
        new SuspiciousUserAgentClassifier(),
        new LargeBodySizeClassifier(),
        new CompromisedPasswordClassifier(weakOrBreachedPwBf, configReader),
        new DisposableEmailClassifier(disposableMxBf, configReader),
        new GeoDiscrepancyClassifier(geoIp4j, configReader),
        new PublicCloudExecutionClassifier(awsIpMembership, gcpIpMembership)
        // new SuspiciousLoginActivityClassifier(geoIp4j)
    );

    CompositeClassifier quarantyneRequestClassifier = new CompositeClassifier(httpRequestClassifierList);

    vertx.deployVerticle(new AdminVerticle(proxyConfig));

    vertx.deployVerticle(() -> new ProxyVerticle(proxyConfig, quarantyneRequestClassifier, configReader),
        new DeploymentOptions().setInstances(numCpus * 2 + 1));

    vertx.deployVerticle(() -> new WarmupVerticle(proxyConfig),
        new DeploymentOptions(),
        warmupVerticle -> {
          vertx.undeploy(warmupVerticle.result());
        });
  }

}
