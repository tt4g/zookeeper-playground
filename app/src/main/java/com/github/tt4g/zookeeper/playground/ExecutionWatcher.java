package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.watcher.WatcherChain;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

@NullMarked
public class ExecutionWatcher implements Watcher {

    private final Logger logger = LoggerFactory.getLogger(ExecutionWatcher.class);

    private final WatcherChain childWatcher;

    private final ZookeeperStateListener zookeeperStateListener;

    private final AtomicBoolean connected;

    ExecutionWatcher(
        WatcherChain childWatcher,
        ZookeeperStateListener zookeeperStateListener
    ) {
        this.childWatcher = childWatcher;
        this.zookeeperStateListener = zookeeperStateListener;
        this.connected = new AtomicBoolean(false);
    }

    @Override
    public void process(WatchedEvent event) {
        this.logger.atDebug()
            .addArgument(event.getState())
            .log("Zookeeper State {}.");

        switch (event.getState()) {
            case Event.KeeperState.SyncConnected:
                if (!this.connected.compareAndExchange(false, true)) {
                    this.zookeeperStateListener.onConnected(false);
                }
                break;

            case Event.KeeperState.ConnectedReadOnly:
                if (!this.connected.compareAndExchange(false, true)) {
                    this.zookeeperStateListener.onConnected(true);
                }
                break;

            case Event.KeeperState.Disconnected:
                this.zookeeperStateListener.onDisconnected();
                break;

            case Event.KeeperState.SaslAuthenticated:
                this.zookeeperStateListener.onSaslAuthenticated();
                break;

            case Event.KeeperState.AuthFailed:
                this.zookeeperStateListener.onAuthFailed();
                break;

            case Event.KeeperState.Expired:
                this.zookeeperStateListener.onSessionExpired();
                System.exit(1);
                break;

            case Event.KeeperState.Closed:
                this.zookeeperStateListener.onClosed();
                break;
        }

        this.childWatcher.process(event);
    }
}
