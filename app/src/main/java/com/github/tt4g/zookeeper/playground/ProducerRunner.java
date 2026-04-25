package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.queue.PushableQueue;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.IntStream;

@NullMarked
public class ProducerRunner implements Runner {
    private final Logger logger = LoggerFactory.getLogger(ProducerRunner.class);

    private final String id;

    private final PushableQueue<String> queue;

    private final int count;

    private final Clock clock;

    private final DateTimeFormatter dateTimeFormatter;

    ProducerRunner(
        PushableQueue<String> queue,
        int count,
        Clock clock,
        DateTimeFormatter dateTimeFormatter
    ) {
        this.id = UUID.randomUUID().toString();
        this.queue = queue;
        this.count = count;
        this.clock = clock;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public void run() {
        this.logger.debug("ProducerRunner::run()");

        IntStream.range(0, this.count).forEach(this::pushElement);
    }

    private void pushElement(int count) {
        var now = ZonedDateTime.now(this.clock);
        var formattedNow = this.dateTimeFormatter.format(now);
        var element = "Id=" + this.id + " Element=" + count + " PushedAt=" + formattedNow;

        try {
            this.queue.push(element);
        } catch (Exception ex) {
            this.logger.atWarn()
                .setCause(ex)
                .addArgument(count)
                .log("Failed to push element. count={}");

            return;
        }

        this.logger.atInfo()
            .addArgument(count)
            .addArgument(element)
            .log("Push element. count={} element={}");
    }

    @Override
    public void stop() {
        this.logger.debug("ProducerRunner::stop()");
    }
}
