package com.github.tt4g.zookeeper.playground.zookeeper;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

@NullMarked
public abstract class AbstractZookeeperTest {
    // Testcontainers singleton container pattern.
    static final ZookeeperContainers ZOOKEEPER_CONTAINERS;

    static {
        ZOOKEEPER_CONTAINERS = new ZookeeperContainers();
        ZOOKEEPER_CONTAINERS.start();
    }

    protected ZookeeperConfig createZookeeperConfig() {
        var zookeeperHost = ZOOKEEPER_CONTAINERS.getZookeeperConnectionString();
        var sessionTimeoutMills = 30_000;
        return new ZookeeperConfig(zookeeperHost, sessionTimeoutMills);
    }

    protected ZookeeperClient createZookeeperClient() throws IOException {
        var watcher =
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // Noop
                }
            };

        return this.createZookeeperClient(watcher);
    }

    protected ZookeeperClient createZookeeperClient(
        Watcher watcher
    ) throws IOException {
        var zookeeperConfig = createZookeeperConfig();
        return ZookeeperClient.create(zookeeperConfig, watcher);
    }

    protected void deleteRecursiveAll(
        ZookeeperClient zookeeperClient,
        ZNodePath pathRoot
    ) throws InterruptedException, KeeperException {
        zookeeperClient.deleteRecursive(pathRoot);
    }
}
