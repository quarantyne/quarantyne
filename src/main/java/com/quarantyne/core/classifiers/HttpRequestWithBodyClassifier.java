package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import java.util.Set;

public interface HttpRequestWithBodyClassifier {
  Set<Label> EMPTY_LABELS = Sets.newHashSet();
  Set<Label> classify(final HttpRequest httpRequest, HttpRequestBody body);
}
