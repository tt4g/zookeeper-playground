package com.github.tt4g.zookeeper.playground.zookeeper.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

@NullMarked
public class ZookeeperConnection {

    private final ZooKeeper zooKeeper;

    private ZookeeperConnection(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    static ZookeeperConnection connect(
        ZookeeperConfig zookeeperConfig,
        Watcher watcher
    ) throws IOException {
        var zookeeper =
            new ZooKeeper(
                zookeeperConfig.zookeeperHost(),
                zookeeperConfig.sessionTimeout(),
                watcher
            );

        return new ZookeeperConnection(zookeeper);
    }

    public void close() throws InterruptedException {
        this.zooKeeper.close();
    }
}
