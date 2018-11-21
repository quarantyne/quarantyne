package com.quarantyne.classifiers;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
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

  public static Label NONE = null;
  public static Label LARGE_BODY = new Label("LBD");
  public static Label FAST_BROWSER = new Label("FAS");
  public static Label COMPROMISED_PASSWORD = new Label("CPW");
  public static Label DISPOSABLE_EMAIL = new Label("DMX");
  public static Label IP_ROTATION = new Label("IPR");
  public static Label SUSPICIOUS_HEADERS = new Label("SHR");
  public static Label SUSPICIOUS_UA = new Label("SUA");
  public static Label PUBLIC_CLOUD_EXECUTION_AWS = new Label("PCX/AWS");
  public static Label PUBLIC_CLOUD_EXECUTION_GCP = new Label("PCX/GCP");
  public static Label IP_COUNTRY_DISCREPANCY = new Label("IPD");
  public static Label SUSPICIOUS_GEO = new Label("SGE");

  public static Set<Label> ALL = Sets.newHashSet(
      LARGE_BODY,
      FAST_BROWSER,
      COMPROMISED_PASSWORD,
      DISPOSABLE_EMAIL,
      IP_ROTATION,
      SUSPICIOUS_HEADERS,
      SUSPICIOUS_UA,
      PUBLIC_CLOUD_EXECUTION_AWS,
      PUBLIC_CLOUD_EXECUTION_GCP,
      IP_COUNTRY_DISCREPANCY,
      SUSPICIOUS_GEO
  );

  private static Set<String> ALL_STRING = ALL.stream()
      .map(Label::getName).collect(Collectors.toSet());

  @Override
  public String toString() {
    return name;
  }

  public static Set<Label> parse(Set<String> labels) throws IllegalArgumentException {
    Set<Label> rep = Sets.newHashSet();
    for (String name: labels) {
      name = name.trim().toUpperCase();
      if (ALL_STRING.contains(name)) {
        rep.add(new Label(name));
      } else {
        throw new IllegalArgumentException("Label " + name + " is not a valid label");
      }
    }
    return rep;
  }

  public static Set<Label> parse(String labels)  throws IllegalArgumentException {
    if (Strings.isNullOrEmpty(labels)) {
      throw new IllegalArgumentException("Labels must be separated by a comma");
    }
    return parse(Sets.newHashSet(labels.split("")));
  }
}
