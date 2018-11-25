package com.quarantyne.classifiers.impl;

import com.quarantyne.classifiers.HttpRequestClassifier;
import com.quarantyne.classifiers.Label;
import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.util.CidrMembership;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

//TODO redo this impl it's awful. Aggregate CIDR providers + use prefix trie to speed up search
@Slf4j
public class PublicCloudExecutionClassifier implements HttpRequestClassifier {

  private final CidrMembership<String> awsMembership;
  private final CidrMembership<String> gcpMembership;

  public PublicCloudExecutionClassifier(
      CidrMembership<String> awsMembership,
      CidrMembership<String> gcpMembership) {
    this.awsMembership = awsMembership;
    this.gcpMembership = gcpMembership;
  }

  @Override
  public Label classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    Optional<String> isAws = awsMembership.get(httpRequest.getRemoteIpAddresses().getOrigin());
    if (isAws.isPresent()) {
      log.debug("{} ip is from AWS", httpRequest.getFingerprint());
      return Label.PUBLIC_CLOUD_EXECUTION;
    }
    Optional<String> isGcp = gcpMembership.get(httpRequest.getRemoteIpAddresses().getOrigin());
    if (isGcp.isPresent()) {
      log.debug("{} ip is from GCP", httpRequest.getFingerprint());
      return Label.PUBLIC_CLOUD_EXECUTION;
    }
    return Label.NONE;
  }
}
