package com.github.tt4g.zookeeper.playground.zookeeper;

import java.util.ArrayList;
import java.util.List;

public class WatcherChainBuilder {
    private final List<ChainWatcher> chainWatchers;

    private WatcherChainBuilder(List<ChainWatcher> chainWatchers) {
        this.chainWatchers = chainWatchers;
    }

    public static WatcherChainBuilder create() {
        return new WatcherChainBuilder(new ArrayList<>());
    }

    public static WatcherChainBuilder of(List<ChainWatcher> watchers) {
        return new WatcherChainBuilder(new ArrayList<>(watchers));
    }

    public WatcherChain build() {
        return new WatcherChainImpl(this.chainWatchers);
    }

    public WatcherChainBuilder add(ChainWatcher chainWatcher) {
        this.chainWatchers.add(chainWatcher);
        return this;
    }
}
