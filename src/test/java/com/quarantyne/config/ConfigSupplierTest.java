package com.quarantyne.config;

import com.google.common.io.Resources;
import com.quarantyne.proxy.ProxyConfig;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.net.URL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ConfigSupplierTest {
  static URL validJson = Resources.getResource("config/valid.json");

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Test
  public void testParseConfig() {
    ProxyConfig proxyConfig = new ProxyConfig();

  }

}
