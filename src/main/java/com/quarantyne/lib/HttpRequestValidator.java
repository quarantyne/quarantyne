package com.quarantyne.lib;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import lombok.NonNull;

public interface HttpRequestValidator {
  default boolean isWriteRequest(@NonNull final HttpRequest httpRequest) {
    Preconditions.checkNotNull(httpRequest);
    return httpRequest.getMethod().equals(HttpRequestMethod.POST)
        || httpRequest.getMethod().equals(HttpRequestMethod.PATCH)
        || httpRequest.getMethod().equals(HttpRequestMethod.PUT);
  }
  default boolean hasBody(@Nullable HttpRequestBody httpRequestBody) {
    return httpRequestBody != null;
  }
}
