package com.quarantyne.db;

import io.vertx.core.Future;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KeyValueStore<K, V> {
  Future<Optional<V>> get(K key);
  Future<Optional<List<V>>> get(Collection<K> keys);
  Future<Void> set(K key, V value);
  Future<Void> set(K key, V value, Duration ttl);
}
