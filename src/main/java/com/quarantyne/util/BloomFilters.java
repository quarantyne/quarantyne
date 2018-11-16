package com.quarantyne.util;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.io.Resources;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BloomFilters {
  /**
   * Make a BF from its serialized form
   * @param resourceName a {@link com.quarantyne.util.BloomFilters} value
   * @return a {@link BloomFilter}
   */
  public static BloomFilter<String> deserialize(String resourceName) throws IOException {
    InputStream is =
        new BufferedInputStream(
            new ByteArrayInputStream(
                Resources.toByteArray(Resources.getResource(resourceName))));
    return BloomFilter.readFrom(is, Funnels.stringFunnel(Charsets.UTF_8));
  }
}
