package com.quarantyne.assets;

public class AssetRegistry {
  public static Asset getCompromisedPasswords() throws AssetException {
    return new Asset("compromised_passwords.dat");
  }

  public static Asset getDisposableEmails() throws AssetException {
    return new Asset("disposable_email.dat");
  }

  public static Asset getAwsIps() throws AssetException {
    return new Asset("aws_ip_ranges.dat");
  }

  public static Asset getGcpIps() throws AssetException {
    return new Asset("gcp_ip_ranges.dat");
  }
}
