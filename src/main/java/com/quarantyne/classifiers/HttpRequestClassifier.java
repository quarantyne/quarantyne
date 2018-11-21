package com.quarantyne.classifiers;

import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpRequestValidator;
import com.quarantyne.lib.HttpResponse;
import javax.annotation.Nullable;
import lombok.NonNull;

public interface HttpRequestClassifier extends HttpRequestValidator {
  @Nullable Label classify(@NonNull final HttpRequest httpRequest, @Nullable final HttpRequestBody body);
  default boolean test(@NonNull final HttpRequest httpRequest, @Nullable final HttpRequestBody body) {
    return true;
  }
  default void record(
      @NonNull final HttpResponse response,
      @NonNull final HttpRequest request,
      @Nullable final HttpRequestBody body) {}
}
