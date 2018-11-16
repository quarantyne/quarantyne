package com.quarantyne.util;

import com.google.common.util.concurrent.AtomicDouble;

public class ExponentialCounter {
  private AtomicDouble value = new AtomicDouble();
  private double step;

  public ExponentialCounter() {
    this.step = 0;
    this.value.set(Math.pow(2, step));
  }

  public void incr() {
    this.step = step + 1;
    this.value.set(Math.pow(2, step));
  }

  public int getValue() {
    return (int)value.get();
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ExponentialCounter{");
    sb.append("value=").append(value);
    sb.append(", step=").append(step);
    sb.append('}');
    return sb.toString();
  }
}
