package com.quarantyne.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Test;

public class CaseInsensitiveStringKVTest {
  @Test
  public void testKV() {
    Map<String, String> map = Maps.newHashMap();
    map.put("a", "a");
    map.put("B", "B");
    CaseInsensitiveStringKV kv = new CaseInsensitiveStringKV(map);
    assertThat(kv.size()).isEqualTo(2);
    assertThat(kv.get("a")).isEqualTo("a");
    assertThat(kv.get("b")).isEqualTo("B");
  }
}
