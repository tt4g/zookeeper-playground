package com.github.tt4g.zookeeper.playground.zookeeper.client;

import com.github.tt4g.zookeeper.playground.ZookeeperConfig;
import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public String createPersistentSequential(ZNodePath path, byte[] data, ACL acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    public String createPersistentSequential(ZNodePath path, byte[] data, List<ACL> acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    public String createEphemeral(ZNodePath path, byte[] data, ACL acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.EPHEMERAL);
    }

    public String createEphemeral(ZNodePath path, byte[] data, List<ACL> acl) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.create(path.getPath(), data, acl, CreateMode.EPHEMERAL);
    }

    public void forceDelete(ZNodePath path) throws InterruptedException, KeeperException {
        this.delete(path, -1);
    }

    public void delete(ZNodePath path, int version) throws InterruptedException, KeeperException {
        this.zookeeperConnection.delete(path.getPath(), version);
    }

    public void deleteRecursive(ZNodePath pathRoot) throws InterruptedException, KeeperException {
        this.zookeeperConnection.deleteRecursive(pathRoot.getPath());
    }

    public void deleteRecursive(ZNodePath pathRoot, int batchSize) throws InterruptedException, KeeperException {
        this.zookeeperConnection.deleteRecursive(pathRoot.getPath(), batchSize);
    }

    public void deleteRecursiveAll(ZNodePath pathRoot) throws InterruptedException, KeeperException {
        this.zookeeperConnection.deleteRecursive(pathRoot.getPath(), -1);
    }

    public boolean existsSync(ZNodePath path, long timeout, TimeUnit unit) throws InterruptedException, KeeperException {
        var waitExistsCallback = new WaitExistsCallback();
        var context = new WaitExistsCallback.Context();
        this.zookeeperConnection.exists(path.getPath(), false, waitExistsCallback, context);

        context.latch.await(timeout, unit);

        var keeperException = context.getException();
        if (keeperException != null) {
            throw keeperException;
        }

        return context.exists.get();
    }

    public byte[] getData(ZNodePath path) throws InterruptedException, KeeperException {
        return this.getData(path, false);
    }

    public byte[] getData(ZNodePath path, boolean watch) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.getData(path.getPath(), watch, null);
    }

    public byte[] getData(ZNodePath path, boolean watch, Stat stat) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.getData(path.getPath(), watch, stat);
    }

    public List<String> getChildren(ZNodePath path) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.getChildren(path.getPath());
    }

    public List<String> getChildren(ZNodePath path, Watcher watcher) throws InterruptedException, KeeperException {
        return this.zookeeperConnection.getChildren(path.getPath(), watcher);
    }

    @Override
    public void close() throws Exception {
        this.zookeeperConnection.close();
    }

    private static class WaitExistsCallback implements AsyncCallback.StatCallback {

        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            var context = (Context) ctx;
            var code = KeeperException.Code.get(rc);
            switch (code) {
                case KeeperException.Code.OK:
                    context.exists.set(true);
                    break;
                case KeeperException.Code.NONODE:
                    context.exists.set(false);
                    break;
                default:
                    context.setException(KeeperException.create(code));
                    break;
            }

            context.latch.countDown();
        }

        private static class Context {
            private final AtomicBoolean exists;
            private final CountDownLatch latch;
            @Nullable
            private KeeperException exception;

            Context() {
                this.exists = new AtomicBoolean(false);
                this.exception = null;
                this.latch = new CountDownLatch(1);
            }

            private synchronized KeeperException getException() {
                return this.exception;
            }

            private synchronized void setException(KeeperException ex) {
                this.exception = ex;
            }
        }
    }
}
