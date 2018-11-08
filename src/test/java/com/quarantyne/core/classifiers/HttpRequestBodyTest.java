package com.quarantyne.core.classifiers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequestBody;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class HttpRequestBodyTest {
  @Test
  public void testGetAny() {
    JsonObject o = new JsonObject();
    o.put("key1", "a");
    o.put("key2", "b");
    o.put("key3", "c");
    HttpRequestBody req = TestHttpRequestBody.make(o);
    assertThat(req.getAny(Sets.newHashSet("key_2", "key2", "KEY2"))).isEqualTo("b");
    assertThat(req.getAny(Sets.newHashSet("key"))).isNull();
    assertThat(req.getAny(Sets.newHashSet())).isNull();
    assertThat(req.getAny(null)).isNull();

  }

}
