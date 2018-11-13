package com.quarantyne.core.classifiers.impl;

import com.quarantyne.core.classifiers.HttpRequestClassifier;
import com.quarantyne.core.classifiers.Label;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.util.CidrMembership;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

//TODO redo this impl it's awful. Aggregate CIDR providers + use prefix trie to speed up search
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
  public Set<Label> classify(HttpRequest httpRequest, @Nullable HttpRequestBody body) {
    Optional<String> isAws = awsMembership.get(httpRequest.getRemoteAddress());
    if (isAws.isPresent()) {
      return Label.PUBLIC_CLOUD_EXECUTION_AWS;
    }
    Optional<String> isGcp = gcpMembership.get(httpRequest.getRemoteAddress());
    if (isGcp.isPresent()) {
      return Label.PUBLIC_CLOUD_EXECUTION_GCP;
    }
    return EMPTY_LABELS;
  }


}
