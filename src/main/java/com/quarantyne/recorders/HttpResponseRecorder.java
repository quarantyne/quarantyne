package com.quarantyne.recorders;

import com.quarantyne.lib.HttpRequest;
import com.quarantyne.lib.HttpRequestBody;
import com.quarantyne.lib.HttpRequestValidator;
import com.quarantyne.lib.HttpResponse;
import javax.annotation.Nullable;

public interface HttpResponseRecorder extends HttpRequestValidator  {
  void record(HttpRequest request, @Nullable  HttpRequestBody body, HttpResponse response);
  default boolean test(HttpRequest request, @Nullable HttpRequestBody body,  HttpResponse response) {
    return true;
  }
}
