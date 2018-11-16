package com.quarantyne.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * A HashMap whose keys are case-insensitive. Used a lot for headers storage and retrieval
 */
@Value
@EqualsAndHashCode(callSuper=false)
public final class CaseInsensitiveStringKV extends HashMap<String, String> {

  private final HashMap<String, String> kv;

  public CaseInsensitiveStringKV(Collection<Entry<String, String>> entries) {
    HashMap<String, String> map = new HashMap<>(entries.size());
    entries.forEach(e -> map.put(e.getKey().toLowerCase(), e.getValue()));
    this.kv = map;
  }


  public CaseInsensitiveStringKV(Map<String, String> kv) {
    this(kv.entrySet());
  }

  @Override
  public int size() {
    return kv.size();
  }

  @Override
  public boolean isEmpty() {
    return kv.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return kv.containsKey(((String)key).toLowerCase());
  }

  @Override
  public boolean containsValue(Object value) {
    return kv.containsValue(value);
  }

  @Override
  public String get(Object key) {
    return kv.get(((String)key).toLowerCase());
  }

  @Override
  public String put(String key, String value) {
    return this.put(key.toLowerCase(), value);
  }

  @Override
  public String getOrDefault(Object key, String defaultValue) {
    return kv.getOrDefault(((String)key).toLowerCase(), defaultValue);
  }


  @Override
  public String putIfAbsent(String key, String value) {
    return kv.putIfAbsent(key.toLowerCase(), value);
  }

  @Override
  public String remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object key, Object value) {
    return kv.remove(((String)key).toLowerCase(), value);
  }

  @Override
  public boolean replace(String key, String oldValue, String newValue) {
    return kv.replace(key.toLowerCase(), oldValue, newValue);
  }

  @Override
  public String replace(String key, String value) {
    return kv.replace(key.toLowerCase(), value);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    return kv.keySet();
  }

  @Override
  public Collection<String> values() {
    return kv.values();
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return kv.entrySet();
  }
}
