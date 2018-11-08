package com.quarantyne.config;

import com.google.common.collect.Sets;
import com.quarantyne.proxy.ProxyConfig;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/*
{
  "login_action": {
    "path": "/login",
    "identifier_param": "email",
    "secret_param": "password"
  },
  "register_action": {
    "path": "/register",
    "identifier_param": "email",
    "secret_param": "password"
  },
  "email_param_keys": ["email", "contact[email]"],
  "country_iso_code_param_keys": ["country_code"],
  "blocked_request_page": "/500.html",
  "blocked_classes": ["pce"],
  "is_disabled": false
}
 */
@Slf4j
public class ConfigReader implements Supplier<Config> {

  private ConfigRetriever configRetriever;

  private static String LOGIN_ACTION = "login_action";
  private static String REGISTER_ACTION = "login_action";
  private static String PATH = "path";
  private static String IDENTIFIER_PARAM = "identifier_param";
  private static String SECRET_PARAM = "secret_param";
  private static String EMAIL_PARAM_KEYS = "email_param_keys";
  private static String COUNTRY_ISO_KEYS = "country_iso_code_param_keys";
  private static String BLOCKED_REQUEST_PAGE = "blocked_request_page";
  private static String BLOCKED_CLASSES = "blocked_classes";
  private static String IS_DISABLED = "is_disabled";

  private AtomicReference<Config> ref = new AtomicReference<>();

  public ConfigReader(Vertx vertx, ProxyConfig proxyConfig) {
    this.ref.set(new Config());
    ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
    ConfigStoreOptions httpStore = makeHttpStore(proxyConfig);
    ConfigStoreOptions fileStore = makeFileStore(proxyConfig);

    if (httpStore != null) {
      log.info("quarantyne configuration found at {}", proxyConfig.getConfigUrl());
      configRetrieverOptions.addStore(httpStore);
    } else if (fileStore != null) {
      log.info("quarantyne configuration found at {}", proxyConfig.getConfigFile());
      configRetrieverOptions.addStore(fileStore);
    }
    configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);

    if (configRetrieverOptions.getStores().size() > 0) {
      configRetriever.getConfig(
          h -> {
            if (h.succeeded()) {
              ref.set(read(h.result()));
              log.info("config will be reloaded every {} ms",
                    configRetrieverOptions.getScanPeriod());
                configRetriever.listen(
                    listen -> {
                      if (listen.getNewConfiguration() != null) {
                        Config config = read(listen.getNewConfiguration());
                        if (config != null) {
                          log.info("new configuration detected [{}], reloading", config.toString());
                          ref.set(config);
                        }
                      } else {
                        log.error("error while loading configuration, skipping update");
                      }
                    });
            } else {
              log.error("failed to load configuration", h.cause());
            }
          });
    } else {
      log.info("Loading default Quarantyne configuration");
    }

  }

  private ConfigStoreOptions makeHttpStore(ProxyConfig proxyConfig) {
    if (proxyConfig.getConfigUrl() != null) {
      URL url;
      try {
        url = new URL(proxyConfig.getConfigUrl());
        return new ConfigStoreOptions()
            .setType("http")
            .setConfig(new JsonObject()
                .put("host", url.getHost())
                .put("port", url.getPort())
                .put("path", url.getPath()));
      } catch (MalformedURLException ex) {
        log.error("{} is not a valid url, skipping...", proxyConfig.getConfigUrl());
      }
    }
    return null;
  }

  private ConfigStoreOptions makeFileStore(ProxyConfig proxyConfig) {
    if (proxyConfig.getConfigFile() != null) {
      return new ConfigStoreOptions()
          .setType("file")
          .setConfig(new JsonObject().put("path", proxyConfig.getConfigFile()));

    }
    return null;
  }

  @Override
  public Config get() {
    return ref.get();
  }

  static Config read(JsonObject newConfig) throws IllegalStateException {
    if (newConfig == null) {
      return null;
    }

    Config.ConfigBuilder updatedConfig = Config.builder();

    // login_action
    Optional.ofNullable(newConfig.getJsonObject(LOGIN_ACTION)).ifPresent(v ->
        updatedConfig.loginAction(getIdentityAction(v))
    );

    // register_action
    Optional.ofNullable(newConfig.getJsonObject(REGISTER_ACTION)).ifPresent(v ->
        updatedConfig.registerAction(getIdentityAction(v))
    );

    // email_param_keys
    Optional.ofNullable(newConfig.getJsonArray(EMAIL_PARAM_KEYS)).ifPresent(v ->
      updatedConfig.emailParamKeys(Sets.newHashSet(v.getList()))
    );

    // country_iso_code_param_keys
    Optional.ofNullable(newConfig.getJsonArray(COUNTRY_ISO_KEYS)).ifPresent(v ->
        updatedConfig.countryIsoCodeParamKeys(Sets.newHashSet(v.getList()))
    );

    // blocked_request_page
    Optional.ofNullable(newConfig.getString(BLOCKED_REQUEST_PAGE)).ifPresent(
        updatedConfig::blockedRequestPage
    );

    // blocked_classes
    Optional.ofNullable(newConfig.getJsonArray(BLOCKED_CLASSES)).ifPresent(v ->
        updatedConfig.blockedClasses(Sets.newHashSet(v.getList()))
    );

    // is_disabled
    Optional.ofNullable(newConfig.getBoolean(IS_DISABLED)).ifPresent(v ->
        updatedConfig.isDisabled(v)
    );

    return updatedConfig.build();
  }

  private static QIdentityAction getIdentityAction(JsonObject o) {
    return new QIdentityAction(
        o.getString(PATH),
        o.getString(IDENTIFIER_PARAM),
        o.getString(SECRET_PARAM)
    );
  }

}
