package com.quarantyne.assets;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

@Slf4j
public class GcpIpRanges extends Asset {

  Resolver resolver;

  public GcpIpRanges() {
    try {
      this.resolver = new SimpleResolver("8.8.8.8");
    } catch (UnknownHostException uex) {
      log.error("cannot instantiate resolver", uex);
    }
  }

  @Override
  public Void write() {
    String answer = lookup("_cloud-netblocks.googleusercontent.com");
    if (!Strings.isNullOrEmpty(answer)) {
      List<String> mergedRecords = Lists.newArrayList();

      parseNetblocks(answer).forEach(netblock -> {
        mergedRecords.addAll(parseNetblock(lookup(netblock)));
      });

      try {
        Files.write(to("gcp_ip_ranges.dat"), mergedRecords);
        log.info("success");
        return null;
      } catch (IOException ex) {
        log.error("cannot write file", ex);
      }
    }
    return null;
  }

  String lookup(String name) {
    Lookup l;
    try {
      l = new Lookup(name, Type.TXT);
      l.setResolver(resolver);
      l.run();
      if (l.getResult() == Lookup.SUCCESSFUL) {
        return l.getAnswers()[0].rdataToString();
      } else {
        log.error("cannot lookup netblocks: []", l.getErrorString());
      }
    } catch (TextParseException ex) {
      log.error("error", ex);
    }
    return null;
  }

  List<String> parseNetblocks(String answer) {
    return splitAndFilter(answer, "include");
  }

  List<String> parseNetblock(String answer) {
    return splitAndFilter(answer, "ip4");
  }

  List<String> splitAndFilter(String answer, String label) {
    return Lists.newArrayList(answer.split(" "))
        .stream()
        .filter(e -> e.startsWith(label))
        .map(e -> e.split(":")[1])
        .collect(Collectors.toList());
  }
}
