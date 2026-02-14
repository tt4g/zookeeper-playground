package com.github.tt4g.zookeeper.playground.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class WatcherChainLoop implements WatcherChain {
    private final List<ChainWatcher> chainWatchers;
    private int currentPosition;

    WatcherChainLoop(List<ChainWatcher> chainWatchers) {
        this.chainWatchers = List.copyOf(chainWatchers);
        this.currentPosition = 0;
    }


    @Override
    public void process(@NonNull WatchedEvent event) {
        if (this.currentPosition >= this.chainWatchers.size()) {
            return;
        }

        ++this.currentPosition;

        final var chainWatcher = this.chainWatchers.get(this.currentPosition);
        chainWatcher.process(event, this);
    }
}
