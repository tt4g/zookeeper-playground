package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.client.ZookeeperClient;
import com.github.tt4g.zookeeper.playground.zookeeper.WatcherChainBuilder;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@NullMarked
public class ExecutionController {

    private final Logger logger = LoggerFactory.getLogger(ExecutionController.class);

    private final ZookeeperClient zookeeperClient;

    private final ExecutionWatcher executionWatcher;

    private ExecutionController(
        ZookeeperClient zookeeperClient,
        ExecutionWatcher executionWatcher
    ) {
        this.zookeeperClient = zookeeperClient;
        this.executionWatcher = executionWatcher;
    }

    public static ExecutionController start(
        ZookeeperConfig zookeeperConfig
    ) throws IOException {
        // TODO: Add child watchers.
        final var watcherChain = WatcherChainBuilder.create().build();
        final var executionWatcher = new ExecutionWatcher(watcherChain);

        final var zookeeperClient =
            ZookeeperClient.create(
                zookeeperConfig,
                executionWatcher
            );

        return new ExecutionController(zookeeperClient, executionWatcher);
    }
}
