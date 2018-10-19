package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequest;
import java.util.Set;

public interface HttpRequestClassifier {
  Set<Label> EMPTY_LABELS = Sets.newHashSet();
  Set<Label> classify(final HttpRequest httpRequest);
}
