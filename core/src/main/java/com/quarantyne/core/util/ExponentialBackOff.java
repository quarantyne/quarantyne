package com.quarantyne.core.util;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class ExponentialBackOff {

  public static ExponentialBackOff DEFAULT =
      new ExponentialBackOff(new ExponentialCounter(), Clock.systemUTC());

  private Clock clock;
  private Instant lastSeenAt;
  private ExponentialCounter penalty;

  @VisibleForTesting
  public ExponentialBackOff(Clock clock) {
    this(new ExponentialCounter(), clock);
  }

  public ExponentialBackOff(ExponentialCounter penalty, Clock clock) {
    this.lastSeenAt = Instant.now(clock);
    this.penalty = penalty;
    this.clock = clock;
  }

  public boolean isBackedOff() {
    return lastSeenAt.plus(Duration.ofSeconds(penalty.getValue())).isAfter(Instant.now(clock));
  }

  public void touch() {
    lastSeenAt = Instant.now(clock);
    penalty.incr();
  }

  @VisibleForTesting
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  public Instant getLastSeenAt() {
    return lastSeenAt;
  }

  public int getPenalty() {
    return penalty.getValue();
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Entry{");
    sb.append("lastSeenAt=").append(lastSeenAt);
    sb.append(", now=").append(Instant.now(clock));
    sb.append(", penalty=").append(penalty);
    sb.append(", isBackedOff=").append(isBackedOff());
    sb.append('}');
    return sb.toString();
  }
}
