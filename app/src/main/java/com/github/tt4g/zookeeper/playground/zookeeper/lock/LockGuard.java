package com.github.tt4g.zookeeper.playground.zookeeper.lock;

import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@NullMarked
public class LockGuard implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(LockGuard.class);

    private final AtomicBoolean closed;

    private final ReentrantLock lock;

    private final ZookeeperClient zookeeperClient;

    private final ZNodePath path;

    private final List<ACL> acl;

    private final LockGuardListener lockGuardListener;

    @Nullable
    private String lockNode;

    public LockGuard(
        ZookeeperClient zookeeperClient,
        ZNodePath path,
        LockGuardListener lockGuardListener
    ) {
        this.closed = new AtomicBoolean(false);
        this.lock = new ReentrantLock();
        this.zookeeperClient = zookeeperClient;
        this.path = path;
        this.acl = ZooDefs.Ids.READ_ACL_UNSAFE;
        this.lockGuardListener = lockGuardListener;
        this.lockNode = null;
    }

    /**
     * @return `true` if the lock acquired, otherwise returns `false`.
     * @throws InterruptedException
     * @throws KeeperException
     */
    public boolean lock() throws InterruptedException, KeeperException {
        this.lock.lock();
        try {
            this.lockNode =
                this.zookeeperClient.createEphemeral(this.path, new byte[]{}, this.acl);
            this.lockGuardListener.lockAcquired();

            this.logger.debug("znode of LockGuard created: {}", this.lockNode);

            return true;
        } catch (KeeperException ex) {
            if (ex.code() == KeeperException.Code.NODEEXISTS) {
                this.lockGuardListener.lockConflict();
                this.logger.debug("znode of LockGuard already exists.", ex);
                return false;
            }

            throw ex;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     *
     * @return `true` if the lock released, otherwise returns `false`.
     * @throws InterruptedException
     * @throws KeeperException
     */
    public boolean unlock() throws InterruptedException, KeeperException {
        if (isClosed()) {
            return false;
        }

        return this.unlockInternal();
    }

    private boolean unlockInternal() throws InterruptedException, KeeperException {
        this.lock.lock();
        try {
            if (this.lockNode == null) {
                // Not locked.
                return false;
            }
            this.zookeeperClient.delete(this.path, -1);
            this.lockNode = null;
            this.lockGuardListener.lockReleased();

            return true;
        } catch (KeeperException ex) {
            if (ex.code() == KeeperException.Code.NONODE) {
                // Node already deleted. Since the lock release was successful.
                // However, failed to unlock operation, so it returns `true`.
                this.logger.debug("znode of LockGuard already deleted.", ex);
                this.lockNode = null;
                return false;
            }

            throw ex;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * @return `true` if locked, otherwise returns `false`.
     */
    public boolean isLocked() {
        this.lock.lock();
        try {
            return this.lockNode != null;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * @return `true` if closed, otherwise returns `false`.
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    public void close() throws Exception {
        if (this.closed.compareAndExchange(false, true)) {
            return;
        }

        try {
            this.unlockInternal();
        } catch (InterruptedException | KeeperException ex) {
            this.logger.warn("Failed to unlock on close", ex);
            throw new IOException(ex);
        }
    }
}
