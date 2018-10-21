package com.quarantyne.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExponentialCounterTest {
  @Test
  public void testExponentialIncrement() {
    ExponentialCounter counter = new ExponentialCounter();
    assertThat(counter.getValue()).isEqualTo(1);
    assertThat(counter.getValue()).isEqualTo(1);
    counter.incr();
    assertThat(counter.getValue()).isEqualTo(2);
    counter.incr();
    assertThat(counter.getValue()).isEqualTo(4);
    counter.incr();
    assertThat(counter.getValue()).isEqualTo(8);
    counter.incr();
    assertThat(counter.getValue()).isEqualTo(16);
    counter.incr();
    assertThat(counter.getValue()).isEqualTo(32);
  }

}
