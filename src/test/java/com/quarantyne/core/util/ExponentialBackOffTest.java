package com.quarantyne.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;

public class ExponentialBackOffTest {

  @Test
  public void testBackOff() {
    Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    ExponentialBackOff es = new ExponentialBackOff(clock);

    assertThat(es.getLastSeenAt()).isEqualTo(Instant.now(clock));
    assertThat(es.getPenalty()).isEqualTo(1);
    assertThat(es.isBackedOff()).isTrue();
    es.touch();
    assertThat(es.getPenalty()).isEqualTo(2);
    es.touch();
    assertThat(es.getPenalty()).isEqualTo(4);
    es.touch();
    assertThat(es.getPenalty()).isEqualTo(8);
    es.setClock(Clock.fixed(Instant.now(clock).plusMillis(8_000), ZoneId.systemDefault()));
    assertThat(es.isBackedOff()).isFalse();

  }

}
