package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.WatcherChain;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ExecutionWatcher implements Watcher {

    private final WatcherChain childWatcher;

    ExecutionWatcher(WatcherChain childWatcher) {
        this.childWatcher = childWatcher;
    }

    @Override
    public void process(WatchedEvent event) {
        // TODO: check Zookeeper events for control application.

        this.childWatcher.process(event);
    }
}
