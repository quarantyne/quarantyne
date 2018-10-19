package com.quarantyne.core.bloom;

import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenBloomFilters {
  public static void main(String...args)  {

    // 10MM breached password sorted by frequency
    // https://gist.github.com/scottlinux/9a3b11257ac575e4f71de811322ce6b3
    BloomFilters.serialize(
        Paths.get(System.getProperty("user.home") + "/quarantyne/passwords.txt"),
        Paths.get("src/main/resources/"+BloomFilters.PASSWORDS_BF_RESOURCE),
        10_000_000,
        0.001
    );


    // disposable email services
    // https://github.com/ivolo/disposable-email-domains
    BloomFilters.serialize(
        Paths.get(System.getProperty("user.home") + "/quarantyne/mx_domains.txt"),
        Paths.get("src/main/resources/"+BloomFilters.MX_DOMAINS_BF_RESOURCE),
        13_671,
        0.001
    );
  }

}
