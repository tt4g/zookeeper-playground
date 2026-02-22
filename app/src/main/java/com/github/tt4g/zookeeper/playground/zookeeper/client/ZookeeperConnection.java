package com.github.tt4g.zookeeper.playground.zookeeper.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.List;

@NullMarked
public class ZookeeperConnection implements AutoCloseable {

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

    public String create(String path, byte[] data, ACL acl, CreateMode createMode) throws InterruptedException, KeeperException {
        return this.create(path, data, List.of(acl), createMode);
    }

    public String create(String path, byte[] data, List<ACL> acl, CreateMode createMode) throws InterruptedException, KeeperException {
        return this.zooKeeper.create(path, data, acl, createMode);
    }

    public void delete(String path, int version) throws InterruptedException, KeeperException {
        this.zooKeeper.delete(path, version);
    }

    @Override
    public void close() throws Exception {
        try {
            this.zooKeeper.close();
        } catch (InterruptedException ex) {
            // Suppressing "try" warning.
            // TIP: The `AutoClosable#close()` method's signature has
            //  `throws Exception`, but the compiler reports a “try” warning
            //  here because `InterruptedException` may be thrown.
            //  The `Zookeeper#close()` method's signature has
            //  `throws InterruptedException`, but it is never thrown.
            //  See: https://github.com/apache/zookeeper/blob/release-3.9.4/zookeeper-server/src/main/java/org/apache/zookeeper/ZooKeeper.java#L137-L144
            throw new IOException(ex);
        }
    }
}
