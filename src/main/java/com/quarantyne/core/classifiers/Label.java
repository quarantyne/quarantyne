package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Value;

@Value
public class Label {
  private String name;

  private Label(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static Set<Label> NONE = Sets.newHashSet();
  public static Set<Label> LARGE_BODY = Sets.newHashSet(new Label("LBD"));
  public static Set<Label> FAST_BROWSER = Sets.newHashSet(new Label("FAS"));
  public static Set<Label> COMPROMISED_PASSWORD = Sets.newHashSet(new Label("CPW"));
  public static Set<Label> DISPOSABLE_EMAIL = Sets.newHashSet(new Label("DMX"));
  public static Set<Label> IP_ROTATION = Sets.newHashSet(new Label("IPR"));
  public static Set<Label> SUSPICIOUS_HEADERS = Sets.newHashSet(new Label("SHR"));
  public static Set<Label> SUSPICIOUS_UA = Sets.newHashSet(new Label("SUA"));
  public static Set<Label> PUBLIC_CLOUD_EXECUTION = Sets.newHashSet(new Label("PCX"));
  public static Set<Label> IP_COUNTRY_DISCREPANCY = Sets.newHashSet(new Label("IPD"));
  public static Set<Label> SUSPICIOUS_GEO = Sets.newHashSet(new Label("SGE"));

  @Override
  public String toString() {
    return name;
  }
}
