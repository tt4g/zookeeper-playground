package com.github.tt4g.zookeeper.playground;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: implementation
@NullMarked
public class ConsumerRunner implements Runner {
    private final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);

    @Override
    public void run() {
        this.logger.debug("ConsumerRunner::run()");
    }

    @Override
    public void stop() {
        this.logger.debug("ConsumerRunner::stop()");
    }
}
