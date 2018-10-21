package com.quarantyne.core.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.quarantyne.core.classifiers.Label;
import org.junit.Test;

public class LabelTest {

  @Test
  public void testLabelEquality() {
    assertThat(Label.COMPROMISED_PASSWORD).isEqualTo(Label.COMPROMISED_PASSWORD);
  }
}
