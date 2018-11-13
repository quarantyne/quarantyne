package com.quarantyne.assets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class AzureIpRanges extends Asset {

  private OkHttpClient okHttpClient;
  private Gson gson;

  private static String URL = "https://download.microsoft.com/download/7/1/D/71D86715-5596-4529-9B13-DA13A5DE5B63/ServiceTags_Public_20181112.json";
  private static Request REQ = new Request.Builder().url(URL).build();

  public AzureIpRanges(OkHttpClient okHttpClient, Gson gson) {
    this.okHttpClient = okHttpClient;
    this.gson = gson;
  }

  @Data
  private static class Property {
    ArrayList<String> addressPrefixes;
  }

  @Data
  private static class Value {
    Property property;
  }

  @Data
  private static class JsonResponse {
    ArrayList<Value> values;
  }

  @Override
  public Void write() {
    Response response;
    try {
      response = okHttpClient.newCall(REQ).execute();
      String bodyString = response.body().string();
      JsonResponse jsonResponse = gson.fromJson(bodyString, JsonResponse.class);

      log.info("skipped");
      return null;
    } catch (IOException ex) {
      log.error("error while retrieving AWS IP range", ex);
      return null;
    }
  }
}
