package com.quarantyne.core.util;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class CaseInsensitiveStringKV extends HashMap<String, String> {
  private final ImmutableSet<Entry<String, String>> entrySet;

  public CaseInsensitiveStringKV(Collection<Entry<String, String>> kv) {
    this.entrySet = ImmutableSet.copyOf(kv
        .stream()
        .map(e ->
            new AbstractMap.SimpleImmutableEntry<>(e.getKey().toLowerCase(), e.getValue())
        )
        .sorted(Comparator.comparing(AbstractMap.SimpleImmutableEntry::getKey))
        .collect(Collectors.toSet()));
  }

  public CaseInsensitiveStringKV(Map<String, String> kv) {
    this(kv.entrySet());
  }
  // used for multipart form

  @Override
  public Set<Entry<String, String>> entrySet() {
    return entrySet;
  }

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String) {
      return super.containsKey(((String) key).toLowerCase());
    } else {
      return super.containsKey(key);
    }
  }

  @Override
  public String get(Object key) {
    if (key instanceof String) {
      return super.get(((String) key).toLowerCase());
    } else {
      return super.get(key);
    }
  }

  @Override
  public String getOrDefault(Object key, String defaultValue) {
    if (key instanceof String) {
      return super.getOrDefault(((String) key).toLowerCase(), defaultValue);
    } else {
      return super.getOrDefault(key, defaultValue);
    }
  }

  @Override
  public String put(String key, String value) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public String remove(Object key) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public void clear() {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public String putIfAbsent(String key, String value) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public boolean remove(Object key, Object value) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public boolean replace(String key, String oldValue, String newValue) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public String replace(String key, String value) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
    throw new IllegalStateException("this class is immutable");
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("CaseInsensitiveStringKV{");
    sb.append("entrySet=").append(Joiner.on(",").withKeyValueSeparator(" -> ").join(entrySet));
    sb.append('}');
    return sb.toString();
  }
}
