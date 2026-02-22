package com.github.tt4g.zookeeper.playground.zookeeper.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.List;

@NullMarked
public class ZookeeperClient implements AutoCloseable {
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

    public String createPersistent(ZNodePath path, byte[] data, ACL acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.PERSISTENT);
    }

    public String createPersistent(ZNodePath path, byte[] data, List<ACL> acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.PERSISTENT);
    }

    public String createEphemeral(ZNodePath path, byte[] data, ACL acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.EPHEMERAL);
    }

    public String createEphemeral(ZNodePath path, byte[] data, List<ACL> acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.EPHEMERAL);
    }

    public void delete(ZNodePath path, int version) throws InterruptedException, KeeperException {
        this.zookeeperConnection.delete(path.getPath(), version);
    }

    @Override
    public void close() throws Exception {
        this.zookeeperConnection.close();
    }
}
