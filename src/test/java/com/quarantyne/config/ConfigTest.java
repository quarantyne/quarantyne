package com.quarantyne.config;

import com.google.common.collect.Sets;
import com.quarantyne.classifiers.Label;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {
  @Test
  public void testDefaultConfigNotBlocking() {
    Config config = new Config();
    assertThat(config.isBlocked(Label.PUBLIC_CLOUD_EXECUTION)).isFalse();
    assertThat(config.isBlocked(Sets.newHashSet())).isFalse();
  }

  @Test
  public void testConfigBlocking() {
    Set<Label> PCX = Sets.newHashSet(Label.PUBLIC_CLOUD_EXECUTION);
    Config config =
        Config.builder().blockedClasses(PCX).build();
    assertThat(config.isBlocked(Label.PUBLIC_CLOUD_EXECUTION)).isTrue();
    assertThat(config.isBlocked(Sets.newHashSet())).isFalse();
    assertThat(config.isBlocked(PCX)).isTrue();
    assertThat(config.isBlocked(Sets.newHashSet(Label.PUBLIC_CLOUD_EXECUTION, Label.FAST_BROWSER)))
        .isTrue();
  }

  @Test
  public void testConfigBlockAll() {
    Set<Label> ALL = Sets.newHashSet(Label.ALL);
    Config config =
        Config.builder().blockedClasses(ALL).build();
    assertThat(config.isBlocked(Label.ALL)).isTrue();
    assertThat(config.isBlocked(Sets.newHashSet())).isFalse();
    assertThat(config.isBlocked(ALL)).isTrue();
    assertThat(config.isBlocked(Sets.newHashSet(Label.FAST_BROWSER)))
        .isTrue();
  }
}
