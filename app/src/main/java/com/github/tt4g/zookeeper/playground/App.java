package com.github.tt4g.zookeeper.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    static void main(String[] args) {
        new App().greeting();
    }

    public void greeting() {
        this.logger.atInfo().log("Hello World!");
    }
}
