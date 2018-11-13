package com.quarantyne.assets;

import com.quarantyne.util.BloomFilters;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompromisedPasswords extends Asset {

  @Override
  public Void write() {
    // 10MM breached password sorted by frequency
    // https://gist.github.com/scottlinux/9a3b11257ac575e4f71de811322ce6b3
    BloomFilters.serialize(
        Paths.get(System.getProperty("user.home") + "/quarantyne/passwords.txt"),
        to("compromised_passwords.dat"),
        10_000_000,
        0.001
    );
    log.info("complete");
    return null;
  }
}
