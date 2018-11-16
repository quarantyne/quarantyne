package com.quarantyne.config;

import com.google.common.base.Strings;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigRetrieverOptionsSupplier implements Supplier<ConfigRetrieverOptions> {

  private final ConfigRetrieverOptions configRetrieverOptions;

  public ConfigRetrieverOptionsSupplier(String configLocation) {
    if (Strings.isNullOrEmpty(configLocation)) {
      throw new IllegalArgumentException("Config location require but null/empty found");
    }
    this.configRetrieverOptions = new ConfigRetrieverOptions();
    configRetrieverOptions.setScanPeriod(Duration.ofMinutes(5).toMillis());
    configRetrieverOptions.addStore(
        fromUrl(configLocation).orElseGet(() -> fromFile(configLocation)));
  }

  public ConfigRetrieverOptions get() {
    return configRetrieverOptions;
  }

  private ConfigStoreOptions fromFile(String configLocation) {
    if (Files.exists(Paths.get(configLocation))) {
      log.info("local configuration file found at {}", configLocation);
      return new ConfigStoreOptions()
          .setType("file")
          .setConfig(new JsonObject().put("path", configLocation));
    }
    throw new IllegalArgumentException("invalid config path: " + configLocation);
  }

  private Optional<ConfigStoreOptions> fromUrl(String configLocation) {
    if (isUrl(configLocation)) {
      try {
        URL url = new URL(configLocation);
        JsonObject config = new JsonObject()
            .put("ssl", configLocation.startsWith("https"))
            .put("keepAlive", false)
            .put("host", url.getHost())
            .put("port", getPort(url.getPort(), url.getProtocol()))
            .put("path", url.getPath());
        ConfigStoreOptions configStoreOptions =
            new ConfigStoreOptions()
                .setType("http")
                .setConfig(config);
        log.info("remote configuration file found at {}", configLocation);

        return Optional.of(configStoreOptions);
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException(configLocation + " is not a valid url, skipping...");
      }
    } else {
      return Optional.empty();
    }
  }

  private int getPort(int port, String scheme) {
    if (port == -1) {
      if (scheme.equals("https")) {
        return 443;
      } else {
        return 80;
      }
    } else {
      return port;
    }
  }

  private boolean isUrl(String str) {
    return str.startsWith("http://") || str.startsWith("https://");
  }
}
