package com.quarantyne.core.recorders;

import com.quarantyne.core.lib.HttpRequest;
import com.quarantyne.core.lib.HttpRequestBody;
import com.quarantyne.core.lib.HttpRequestValidator;
import com.quarantyne.core.lib.HttpResponse;
import javax.annotation.Nullable;

public interface HttpResponseRecorder extends HttpRequestValidator  {
  void record(HttpRequest request, @Nullable  HttpRequestBody body, HttpResponse response);
  default boolean test(HttpRequest request, @Nullable HttpRequestBody body,  HttpResponse response) {
    return true;
  }
}
