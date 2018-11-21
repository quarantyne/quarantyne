package com.quarantyne.classifiers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LabelTest {

  @Test
  public void testLabelEquality() {
    assertThat(Label.COMPROMISED_PASSWORD).isEqualTo(Label.COMPROMISED_PASSWORD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalid() {
    Label.parse("a,b,c");
  }
}
