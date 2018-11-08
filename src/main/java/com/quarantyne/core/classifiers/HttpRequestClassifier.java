package com.quarantyne.core.classifiers;

import com.google.common.collect.Sets;
import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.lib.HttpRequestValidator;
import com.quarantyne.core.lib.HttpResponse;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NonNull;

public interface HttpRequestClassifier extends HttpRequestValidator  {
  Set<Label> EMPTY_LABELS = Sets.newHashSet();
  Set<Label> classify(@NonNull final HttpRequest httpRequest, @Nullable final HttpRequestBody body);
  default boolean test(@NonNull final HttpRequest httpRequest, @Nullable final HttpRequestBody body) {
    return true;
  }
  default void record(
      @NonNull final HttpResponse response,
      @NonNull final HttpRequest request,
      @Nullable final HttpRequestBody body) {}
}
