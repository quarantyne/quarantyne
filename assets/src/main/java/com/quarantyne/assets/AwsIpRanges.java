package com.quarantyne.assets;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https://linuxfreelancer.com/cloud-service-providers-ip-ranges-aws-azure-and-gcp
 */
@Slf4j
public class AwsIpRanges extends Asset {

  @Data
  private static class JsonResponse {
    ArrayList<Prefix> prefixes;
  }

  @Data
  private static class Prefix {
    String ip_prefix;
  }

  private OkHttpClient okHttpClient;
  private Gson gson;

  private static String URL = "https://ip-ranges.amazonaws.com/ip-ranges.json";
  private static Request REQ = new Request.Builder().url(URL).build();

  public AwsIpRanges(OkHttpClient okHttpClient, Gson gson) {
    this.okHttpClient = okHttpClient;
    this.gson = gson;
  }

  @Override
  public Void write() {
    Response response;
    try {
      response = okHttpClient.newCall(REQ).execute();
      String bodyString = response.body().string();
      JsonResponse jsonResponse = gson.fromJson(bodyString, JsonResponse.class);

      Files.write(
          to("aws_ip_ranges.dat"),
          jsonResponse.getPrefixes().stream().map(x -> x.ip_prefix).collect(Collectors.toList())
      );

      log.info("complete");
      return null;
    } catch (IOException ex) {
      log.error("error while retrieving AWS IP range", ex);
      return null;
    }
  }
}
