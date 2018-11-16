package com.quarantyne.classifiers.impl;

import com.quarantyne.config.Config;
import java.util.function.Supplier;

class AbstractClassifierTest {
  Supplier<Config> config = () -> new Config();

}
