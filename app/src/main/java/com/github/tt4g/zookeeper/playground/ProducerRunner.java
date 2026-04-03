package com.github.tt4g.zookeeper.playground;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: implementation
@NullMarked
public class ProducerRunner implements Runner {
    private final Logger logger = LoggerFactory.getLogger(ProducerRunner.class);

    @Override
    public void run() {
        this.logger.debug("ProducerRunner::run()");
    }

    @Override
    public void stop() {
        this.logger.debug("ProducerRunner::stop()");
    }
}
