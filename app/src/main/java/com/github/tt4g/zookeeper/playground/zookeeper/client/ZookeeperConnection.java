package com.github.tt4g.zookeeper.playground.zookeeper.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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

    public void deleteRecursive(String pathRoot) throws InterruptedException, KeeperException {
        ZKUtil.deleteRecursive(this.zooKeeper, pathRoot);
    }

    public void deleteRecursive(String pathRoot, int batchSize) throws InterruptedException, KeeperException {
        ZKUtil.deleteRecursive(this.zooKeeper, pathRoot, batchSize);
    }

    public void deleteRecursive(
        String pathRoot,
        AsyncCallback.VoidCallback callback,
        Object context) throws InterruptedException, KeeperException {
        ZKUtil.deleteRecursive(this.zooKeeper, pathRoot, callback, context);
    }

    public void exists(String path, Watcher watcher) throws InterruptedException, KeeperException {
        this.zooKeeper.exists(path, watcher);
    }

    public void exists(String path, boolean watch) throws InterruptedException, KeeperException {
        this.zooKeeper.exists(path, watch);
    }

    public void exists(String path, Watcher watcher, AsyncCallback.StatCallback cb, Object ctx) {
        this.zooKeeper.exists(path, watcher, cb, ctx);
    }

    public void exists(String path, boolean watch, AsyncCallback.StatCallback cb, Object ctx) {
        this.zooKeeper.exists(path, watch, cb, ctx);
    }

    public byte[] getData(String path, boolean watch, @Nullable Stat stat) throws InterruptedException, KeeperException {
        return this.zooKeeper.getData(path, watch, stat);
    }

    public List<String> getChildren(String path) throws InterruptedException, KeeperException {
        return this.zooKeeper.getChildren(path, null);
    }

    public List<String> getChildren(String path, Watcher watcher) throws InterruptedException, KeeperException {
        return this.zooKeeper.getChildren(path, watcher);
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
