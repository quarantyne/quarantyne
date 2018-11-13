package com.quarantyne.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.io.Resources;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Make creating, serializing and deserializing BFs easy.
 */
@Slf4j
public class BloomFilters {
  private static Funnel<CharSequence> FUNNEL = Funnels.stringFunnel(Charsets.UTF_8);

  /**
   * Read a text file from disk, make a BF from it (1 entry per line) then serialize
   * the backing bitarray to disk
   * @param sourcePath The source path of text file used to core the BF from. 1 entry per line
   * @param destPath The serialized form of the bit array backing the BF
   * @param size Size of the BF
   */
  public static void serialize(
      Path sourcePath,
      Path destPath,
      int size,
      double fpp) {
    File sourceFile = sourcePath.toFile();
    File destFile = destPath.toFile();
    try {
      destFile.createNewFile();
      destFile.setWritable(true);
    } catch (IOException ioex) {
      log.error("cannot create " + destPath.toAbsolutePath(), ioex);
      System.exit(-1);
    }
    Preconditions.checkState(
        sourceFile.exists() && sourceFile.canRead(),
        "source %s must exist and be readable", sourcePath.toAbsolutePath());
    Preconditions.checkState(
        destFile.canWrite(),
        "destination %s must be writable", destPath.toAbsolutePath());
    Preconditions.checkState(size > 0, "bloom filter size must be > 0");
    Preconditions.checkState(fpp > 0 && fpp < 1, "0 < fpp < 1 ");

    long start = System.currentTimeMillis();
    log.debug("creating bloom filter for {}", sourcePath);
    BloomFilter<String> bloomFilter = BloomFilter.create(
        FUNNEL,
        size,
        fpp);
    ScheduledExecutorService t = Executors.newSingleThreadScheduledExecutor();
    t.scheduleAtFixedRate(() -> {
      log.debug("bloom filter size {}", bloomFilter.approximateElementCount());
    },0, 1, TimeUnit.SECONDS);

    try (Stream<String> stream = Files.lines(sourcePath)) {
      stream.forEach(e -> bloomFilter.put(e.trim()));
    } catch (IOException ioex) {
      log.error(sourcePath.toString() , ioex);
    } finally{
      t.shutdown();
    }

    log.debug("created bloom filter in {} secs", (System.currentTimeMillis() - start) / 1000);
    log.debug("approximate element count {}", bloomFilter.approximateElementCount());
    log.debug("expected false positive probability {}", bloomFilter.expectedFpp());

    start = System.currentTimeMillis();
    log.debug("starting serialization of bloom filter {} ...", sourcePath);

    try(BufferedOutputStream byteArrayOutputStream =
        new BufferedOutputStream(new FileOutputStream(destFile))) {
      bloomFilter.writeTo(byteArrayOutputStream);
    } catch (IOException ioex) {
      log.error(sourcePath.toString(), ioex);
    }

    log.debug("serialized bloom filter in {} ms", (System.currentTimeMillis() - start) );
  }

  /**
   * Make a BF from its serialized form
   * @param resourceName a {@link BloomFilters} value
   * @return a {@link BloomFilter}
   */
  public static BloomFilter<String> deserialize(String resourceName) throws IOException{
    InputStream is =
        new BufferedInputStream(
            new ByteArrayInputStream(
                Resources.toByteArray(Resources.getResource(resourceName))));
    return BloomFilter.readFrom(is, FUNNEL);
  }
}
