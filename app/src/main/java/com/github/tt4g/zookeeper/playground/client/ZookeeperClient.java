package com.github.tt4g.zookeeper.playground.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import org.apache.zookeeper.WatchedEvent;
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
        ZookeeperConfig zookeeperConfig
    ) throws IOException {
        var zookeeperConnection =
            ZookeeperConnection.connect(
                zookeeperConfig,
                // TODO
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                    }
                }
            );

        return new ZookeeperClient(zookeeperConnection);
    }

    public void close() throws Exception {
        this.zookeeperConnection.close();
    }
}
