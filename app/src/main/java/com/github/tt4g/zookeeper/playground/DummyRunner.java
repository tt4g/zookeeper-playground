package com.github.tt4g.zookeeper.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DummyRunner implements Runner {
    private final Logger logger = LoggerFactory.getLogger(DummyRunner.class);

    @Override
    public void run() {
        this.logger.debug("DummyRunner::run()");
    }

    @Override
    public void stop() {
        this.logger.debug("DummyRunner::stop()");
    }
}
