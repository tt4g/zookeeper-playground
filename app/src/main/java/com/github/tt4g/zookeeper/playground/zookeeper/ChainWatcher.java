package com.github.tt4g.zookeeper.playground.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.jspecify.annotations.NonNull;

public interface ChainWatcher {

    /**
     * Process {@link WatchedEvent} or call the next
     * {@link org.apache.zookeeper.Watcher} via {@link WatcherChain}.
     *
     * @param event
     * @param watcherChain
     */
    void process(@NonNull WatchedEvent event, @NonNull WatcherChain watcherChain);
}
