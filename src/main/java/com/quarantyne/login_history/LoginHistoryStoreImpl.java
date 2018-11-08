package com.quarantyne.login_history;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginHistoryStoreImpl implements LoginHistoryStore {
  private final Map<String, List<LoginEvent>> store = new HashMap<>();
  private static final List<LoginEvent> EMPTY = Lists.newArrayList();

  @Override
  public void registerLogin(String loginIdentifier, LoginEvent loginEvent) {
    if (Strings.isNullOrEmpty(loginIdentifier)) {
      return;
    }
    List<LoginEvent> logins = store.get(loginIdentifier);
    if (logins != null) {
      store.get(loginIdentifier).add(loginEvent);
    } else {
      store.put(loginIdentifier, Lists.newArrayList(loginEvent));
    }
  }

  @Override
  public List<LoginEvent> getLastLogins(String loginIdentifier, int count) {
    if (Strings.isNullOrEmpty(loginIdentifier)) {
      return EMPTY;
    }
    return store.getOrDefault(loginIdentifier, EMPTY).subList(0, count);
  }
}
