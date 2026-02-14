package com.github.tt4g.zookeeper.playground.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import org.apache.zookeeper.Watcher;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

@NullMarked
public class ZookeeperClient {
    private final ZookeeperConnection zookeeperConnection;

    private ZookeeperClient(ZookeeperConnection zookeeperConnection) {
        this.zookeeperConnection = zookeeperConnection;
    }

    public static ZookeeperClient create(
        ZookeeperConfig zookeeperConfig,
        Watcher watcher
    ) throws IOException {
        var zookeeperConnection =
            ZookeeperConnection.connect(
                zookeeperConfig,
                watcher
            );

        return new ZookeeperClient(zookeeperConnection);
    }

    public void close() throws Exception {
        this.zookeeperConnection.close();
    }
}
