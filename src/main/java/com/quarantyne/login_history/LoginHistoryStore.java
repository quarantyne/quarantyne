package com.quarantyne.login_history;

import java.util.List;

public interface LoginHistoryStore {
  void registerLogin(String loginIdentifier, LoginEvent loginEvent);
  List<LoginEvent> getLastLogins(String loginIdentifier, int count);
}