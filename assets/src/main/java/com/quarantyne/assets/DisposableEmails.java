package com.quarantyne.assets;

import com.quarantyne.util.BloomFilters;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableEmails extends Asset {

  @Override
  public Void write() {
    // disposable email services
    // https://github.com/ivolo/disposable-email-domains
    BloomFilters.serialize(
        Paths.get(System.getProperty("user.home") + "/quarantyne/mx_domains.txt"),
        to("disposable_email.dat"),
        13_671,
        0.001
    );
    log.info("complete");
    return null;
  }
}
