package com.quarantyne.util;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.quarantyne.assets.Asset;
import com.quarantyne.assets.AssetException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BloomFilters {
  /**
   * Make a BF from its serialized form
   * @param asset
   * @return a {@link BloomFilter}
   */
  public static BloomFilter<String> deserialize(Asset asset) throws AssetException {
    try {
      return BloomFilter.readFrom(asset.getBytes(), Funnels.stringFunnel(Charsets.UTF_8));
      } catch (IOException ioex) {
      throw new AssetException(ioex);
    }
  }
}
