package com.quarantyne.core.lib;

import com.google.common.collect.Lists;
import com.quarantyne.core.util.CidrMembership;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CidrMembershipTest {
  @Test
  public void test() {
    List<String> blocks = Lists.newArrayList("10.0.0.0/24", "50.2.0.0/18");
    CidrMembership<String> cidrMembership = new CidrMembership<>(blocks, "test");

    assertThat(cidrMembership.get("10.0.0.0")).contains("test");
    assertThat(cidrMembership.get("10.0.0.255")).contains("test");
    assertThat(cidrMembership.get("10.1.0.0")).isEmpty();

    assertThat(cidrMembership.get("50.2.0.0")).contains("test");
    assertThat(cidrMembership.get("50.2.10.0")).contains("test");
    assertThat(cidrMembership.get("50.2.63.255")).contains("test");

    assertThat(cidrMembership.get("50.2.64.0")).isEmpty();
  }
}
