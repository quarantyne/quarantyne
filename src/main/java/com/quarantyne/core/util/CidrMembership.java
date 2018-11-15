package com.quarantyne.core.util;

import com.google.common.io.Resources;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

// naive. update to prefix trie at some point
// upgrade to https://vincent.bernat.ch/en/blog/2017-ipv4-route-lookup-linux
@Slf4j
public class CidrMembership<T> {
  @Value
  static class CidrBlock<T> {
    long ipRangeStart;
    int cidrBlockSize;
    String firstQuad;
    T value;


    boolean includes(Long ip) {
      return ipRangeStart <= ip && ip < (ipRangeStart + cidrBlockSize);
    }

  }
  private Map<String, List<CidrBlock<T>>> map;

  private static String IP_SEP = "\\.";
  private static String CIDR_SEP = "/";
  private final T value;


  public CidrMembership(Iterable<String> blocks, T value)  {
    this.map = new HashMap<>();
    this.value = value;

    List<CidrBlock<T>> geoIpRangeRecordList;
    CidrBlock<T> cidrBlock;
    for (String block: blocks) {
      cidrBlock = parse(block);
      if (map.containsKey(cidrBlock.getFirstQuad())) {
        geoIpRangeRecordList = map.get(cidrBlock.getFirstQuad());
        geoIpRangeRecordList.add(cidrBlock);
      } else {
        geoIpRangeRecordList = new ArrayList<>();
        geoIpRangeRecordList.add(cidrBlock);
        map.put(cidrBlock.getFirstQuad(), geoIpRangeRecordList);
      }
    }
  }

  public CidrMembership(String resource, T value) {
    this.map = new HashMap<>();
    this.value = value;
    Reader file;
    try {
      file = new InputStreamReader(
          new ByteArrayInputStream(
              Resources.toByteArray(Resources.getResource(resource))));
    } catch (IOException ex) {
        throw new IllegalArgumentException("cannot find resource " + resource);
    }
    try (BufferedReader reader = new BufferedReader(file)) {
      List<CidrBlock<T>> geoIpRangeRecordList;
      CidrBlock<T> cidrBlock;
      String line;
      while ((line = reader.readLine()) != null) {
        cidrBlock = parse(line);
        if (map.containsKey(cidrBlock.getFirstQuad())) {
          geoIpRangeRecordList = map.get(cidrBlock.getFirstQuad());
          geoIpRangeRecordList.add(cidrBlock);
        } else {
          geoIpRangeRecordList = new ArrayList<>();
          geoIpRangeRecordList.add(cidrBlock);
          map.put(cidrBlock.getFirstQuad(), geoIpRangeRecordList);
        }
      }
    } catch (IOException ioex) {
      log.error("error while creating ", ioex);
    }
  }

  public Optional<T> get(String ip) {
    if (!isValidIp(ip)) {
      return Optional.empty();
    }
    String key = ip.split("\\.")[0];
    List<CidrBlock<T>> shard = map.get(key);
    Optional<T> result = Optional.empty();
    if (shard != null) {
      for (CidrBlock<T> record: shard) {
        if (record.includes(ipToLong(ip))) {
          result = Optional.of(record.getValue());
          break;
        }
      }
    }
    return result;
  }

  boolean isValidIp(String ip) {
    if (null == ip || ip.isEmpty()) {
      return false;
    }
    if (ip.length() < 7 || ip.length() > 15) {
      return false;
    }
    int dotCount = 0;
    for(char c: ip.toCharArray()) {
      if (c == '.') {
        dotCount += 1;
      }
    }
    return dotCount == 3;
  }

  CidrBlock<T> parse(String str) {
    String[] ipChunks = str.split(CIDR_SEP);
    String[] quads = ipChunks[0].split(IP_SEP);

    int cidrBlockSize = getCidrBlockSize(Integer.parseInt(ipChunks[1]));

    return new CidrBlock<T>(ipToLong(quads), cidrBlockSize, quads[0], this.value);
  }

  long ipToLong(String[] ipAddressInArray) {
    long result = 0;
    for (int i = 3; i >= 0; i--) {
      long ip = Long.parseLong(ipAddressInArray[3 - i]);
      result |= ip << (i * 8);
    }
    return result;
  }

  long ipToLong(String ipAddress) {
    return ipToLong(ipAddress.trim().split("\\."));
  }

  int getCidrBlockSize(int cidr) {
    if (0 < cidr  && cidr <= 32) {
      return (int) Math.pow(2, 32 - cidr);
    } else {
      return -1;
    }
  }
}
