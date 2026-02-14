package com.github.tt4g.zookeeper.playground.zookeeper.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.jspecify.annotations.NonNull;

public interface WatcherChain {
    /**
     * Process next {@link org.apache.zookeeper.Watcher#process(WatchedEvent)}.
     *
     * @param event
     */
    void process(@NonNull WatchedEvent event);
}
