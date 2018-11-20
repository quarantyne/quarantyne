package com.quarantyne.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import org.junit.Test;

public class RemoteIpAddressesTest {
  private static RemoteIpAddresses A
      = new RemoteIpAddresses("2.2.2.2", Sets.newHashSet("1.2.3.4", "2.3.4.5"));
  private static RemoteIpAddresses B
      = new RemoteIpAddresses("2.2.2.2", Sets.newHashSet("2.3.4.5", "1.2.3.4"));
  private static RemoteIpAddresses C
      = new RemoteIpAddresses("2.2.2.3", Sets.newHashSet("2.3.4.5", "1.2.3.4"));
  @Test
  public void testEquality() {
    assertThat(A).isEqualTo(B);
  }

  @Test
  public void testNotEquals() {
    assertThat(A).isNotEqualTo(C);
    assertThat(B).isNotEqualTo(C);
  }

}
