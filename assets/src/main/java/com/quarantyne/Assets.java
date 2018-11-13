package com.quarantyne;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.quarantyne.assets.Asset;
import com.quarantyne.assets.AwsIpRanges;
import com.quarantyne.assets.AzureIpRanges;
import com.quarantyne.assets.CompromisedPasswords;
import com.quarantyne.assets.DisposableEmails;
import com.quarantyne.assets.GcpIpRanges;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Slf4j
public class Assets {

  public static void main(String...args) {
    Gson gson = new Gson();
    OkHttpClient okHttpClient = new OkHttpClient();

    List<CompletableFuture<Void>> assets =  Lists.newArrayList(
        register(new AwsIpRanges(okHttpClient, gson)),
        register(new AzureIpRanges(okHttpClient, gson)),
        register(new CompromisedPasswords()),
        register(new DisposableEmails()),
        register(new GcpIpRanges())
    );

    CompletableFuture
        .allOf(assets.toArray(new CompletableFuture[assets.size()]))
        .exceptionally(e -> {
          log.error("error while producing asset", e);
          return null;
        })
        .join();

  }

  private static CompletableFuture<Void> register(Asset task) {
    return CompletableFuture.supplyAsync(() -> task.write());
  }
}
