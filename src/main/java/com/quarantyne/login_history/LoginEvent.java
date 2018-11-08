package com.quarantyne.login_history;

import java.time.Instant;
import javax.annotation.Nullable;
import lombok.Value;

@Value
public class LoginEvent {
  Instant timestamp;
  String ip;
  @Nullable  String countryIsoCode;
}
