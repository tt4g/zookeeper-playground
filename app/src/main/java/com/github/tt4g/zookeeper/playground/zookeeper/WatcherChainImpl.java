package com.github.tt4g.zookeeper.playground.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
class WatcherChainImpl implements WatcherChain {
    private final List<ChainWatcher> chainWatchers;

    WatcherChainImpl(List<ChainWatcher> chainWatchers) {
        this.chainWatchers = List.copyOf(chainWatchers);
    }

    @Override
    public void process(@NonNull WatchedEvent event) {
        final var watcherChainLoop = new WatcherChainLoop(this.chainWatchers);
        watcherChainLoop.process(event);
    }
}
