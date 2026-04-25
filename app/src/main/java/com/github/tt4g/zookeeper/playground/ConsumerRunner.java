package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.queue.LeaseElement;
import com.github.tt4g.zookeeper.playground.zookeeper.queue.LeaseableQueue;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@NullMarked
public class ConsumerRunner implements Runner {
    private final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);

    private final LeaseableQueue<String> queue;

    private final int count;

    ConsumerRunner(
        LeaseableQueue<String> queue,
        int count
    ) {
        this.queue = queue;
        this.count = count;
    }

    @Override
    public void run() {
        this.logger.debug("ConsumerRunner::run()");

        for (int i = 0; i < this.count; ++i) {
            var consumedElement = this.popElement(i);
            if (consumedElement.isPresent()) {
                var element = consumedElement.get();
                this.logger.atInfo()
                    .addArgument(i)
                    .addArgument(element)
                    .log("Consume element. count={} element={}");

            } else {
                this.logger.atInfo()
                    .addArgument(i)
                    .log("No more element. count={}");
                break;
            }
        }
    }

    private Optional<String> popElement(int count) {
        this.logger.atDebug()
            .addArgument(count)
            .log("Pop Element. count={}");

        try {
            return this.queue.lease().map(leaseElement -> {
                var value = leaseElement.value();
                this.tryAck(leaseElement, count);
                return value;
            });

        } catch (Exception ex) {
            this.logger.atWarn()
                .setCause(ex)
                .addArgument(count)
                .log("Failed to pop element. count={}");

            return Optional.empty();
        }
    }

    private void tryAck(LeaseElement<? extends Object> leaseElement, int count) {
        try {
            leaseElement.ack();
        } catch (Exception ex) {
            this.logger.atWarn()
                .setCause(ex)
                .addArgument(count)
                .log("Failed to ack. count={}");

            this.tryPutBack(leaseElement, count);
        }
    }

    private void tryPutBack(LeaseElement<? extends Object> leaseElement, int count) {
        try {
            leaseElement.putBack();
        } catch (Exception ex) {
            this.logger.atWarn()
                .setCause(ex)
                .addArgument(count)
                .log("Failed to put back. count={}");
        }
    }

    @Override
    public void stop() {
        this.logger.debug("ConsumerRunner::stop()");
    }
}
