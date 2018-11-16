package com.quarantyne.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class ConfigRetrieverOptionsSupplierTest {
  @Test
  public void testInvalidLocation() {
    for(String x: Sets.newHashSet(null, "", "abc", "http:/broken")) {
      try {
        new ConfigRetrieverOptionsSupplier(x);
        Assert.fail();
      } catch (IllegalArgumentException ex) {}
    }
  }

  @Test
  public void testReadLocalConfig() {
    Path p = Paths.get("src/test/resources/config/valid.json");
    String absPath = p.toAbsolutePath().toString();
    ConfigRetrieverOptionsSupplier configRetrieverOptionsSupplier = new ConfigRetrieverOptionsSupplier(absPath);
    String foundConfigPath =
        getStore(configRetrieverOptionsSupplier.get()).toJson().getJsonObject("config").getString("path");
    assertThat(foundConfigPath).isEqualTo(absPath);
  }

  @Test
  public void testReadHttpConfig() {
    String configUrl = "http://s3.example.com/config.json";
    ConfigRetrieverOptionsSupplier configRetrieverOptionsSupplier = new ConfigRetrieverOptionsSupplier(configUrl);
    JsonObject res = getStore(configRetrieverOptionsSupplier.get()).toJson().getJsonObject("config");
    assertThat(res.getString("host")).isEqualTo("s3.example.com");
    assertThat(res.getString("path")).isEqualTo("/config.json");
    assertThat(res.getInteger("port")).isEqualTo(80);
  }

  @Test
  public void testReadHttpsConfig() {
    String configUrl = "https://s3.example.com/config.json";
    ConfigRetrieverOptionsSupplier configRetrieverOptionsSupplier = new ConfigRetrieverOptionsSupplier(configUrl);
    JsonObject res = getStore(configRetrieverOptionsSupplier.get()).toJson().getJsonObject("config");
    assertThat(res.getString("host")).isEqualTo("s3.example.com");
    assertThat(res.getString("path")).isEqualTo("/config.json");
    assertThat(res.getInteger("port")).isEqualTo(443);
  }

  ConfigStoreOptions getStore(ConfigRetrieverOptions configStoreOptionsList) {
    return configStoreOptionsList.getStores().get(0);
  }

}
