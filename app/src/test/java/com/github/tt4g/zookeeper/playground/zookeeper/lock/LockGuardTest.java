package com.github.tt4g.zookeeper.playground.zookeeper.lock;

import com.github.tt4g.zookeeper.playground.zookeeper.AbstractZookeeperTest;
import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class LockGuardTest extends AbstractZookeeperTest {

    private ZNodePath testNodePath;

    private ZookeeperClient zookeeperClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException, KeeperException {
        this.testNodePath = ZNodePath.root().join("LockGuardTest");
        this.zookeeperClient = this.createZookeeperClient();

        this.zookeeperClient.createPersistent(
            this.testNodePath,
            new byte[]{},
            ZooDefs.Ids.OPEN_ACL_UNSAFE
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        this.deleteRecursiveAll(
            this.zookeeperClient,
            this.testNodePath
        );

        this.zookeeperClient.close();
    }

    @Test
    @Timeout(15)
    void testLock() throws InterruptedException, KeeperException {
        var znodePath = this.testNodePath.join("lock");
        var lockState = new LockState(1);
        var lockGuard = new LockGuard(this.zookeeperClient, znodePath, lockState);

        lockGuard.lock();
        lockState.await(10, TimeUnit.SECONDS);

        assertThat(lockGuard.isLocked()).isTrue();
        assertThat(lockState.acquired).isTrue();
        assertThat(lockState.released).isFalse();
        assertThat(lockState.conflict).isFalse();

        var existsZnodePath = this.zookeeperClient.existsSync(znodePath, 5, TimeUnit.SECONDS);
        assertThat(existsZnodePath).isTrue();
    }

    @Test
    @Timeout(15)
    void testUnlock() throws InterruptedException, KeeperException {
        var znodePath = this.testNodePath.join("unlock");
        var lockState = new LockState(1);
        var lockGuard = new LockGuard(this.zookeeperClient, znodePath, lockState);

        lockGuard.lock();
        lockGuard.unlock();

        lockState.await(10, TimeUnit.SECONDS);
        assertThat(lockGuard.isLocked()).isFalse();
        assertThat(lockState.acquired).isTrue();
        assertThat(lockState.released).isTrue();
        assertThat(lockState.conflict).isFalse();

        var existsZnodePath =
            this.zookeeperClient.existsSync(znodePath, 5, TimeUnit.SECONDS);
        assertThat(existsZnodePath).isFalse();
    }

    @Test
    @Timeout(20)
    void testConflict() throws IOException, InterruptedException, KeeperException {
        var znodePath = this.testNodePath.join("conflict");
        var lockState = new LockState(1);
        var lockGuard = new LockGuard(this.zookeeperClient, znodePath, lockState);

        lockGuard.lock();
        lockState.await(10, TimeUnit.SECONDS);
        assertThat(lockGuard.isLocked()).isTrue();

        var conflictZookeeperClient = this.createZookeeperClient();
        var conflictLockState = new LockState(1);
        var conflictLockGuard =
            new LockGuard(conflictZookeeperClient, znodePath, conflictLockState);

        conflictLockGuard.lock();
        conflictLockState.await(10, TimeUnit.SECONDS);
        assertThat(conflictLockGuard.isLocked()).isFalse();
        assertThat(conflictLockGuard.isClosed()).isFalse();
        assertThat(conflictLockState.acquired).isFalse();
        assertThat(conflictLockState.conflict).isTrue();
        assertThat(conflictLockState.released).isFalse();
    }

    @Test
    @Timeout(15)
    void testClose() throws Exception {
        var znodePath = this.testNodePath.join("close");
        var lockState = new LockState(1);
        var lockGuard = new LockGuard(this.zookeeperClient, znodePath, lockState);

        lockGuard.lock();
        lockState.await(10, TimeUnit.SECONDS);
        assertThat(lockGuard.isLocked()).isTrue();
        assertThat(lockState.released).isFalse();

        lockGuard.close();
        assertThat(lockGuard.isLocked()).isFalse();
        assertThat(lockState.released).isTrue();

        var existsZnodePath =
            this.zookeeperClient.existsSync(znodePath, 5, TimeUnit.SECONDS);
        assertThat(existsZnodePath).isFalse();
    }

    @Test
    @Timeout(30)
    void testCloseZookeeperClient() throws Exception {
        var znodePath = this.testNodePath.join("closeZookeeperClient");

        var waitZookeeperClose = new WaitZookeeperClose();
        var firstZookeeperClient = this.createZookeeperClient(waitZookeeperClose);
        var firstLockState = new LockState(1);
        var firstLockGuard = new LockGuard(firstZookeeperClient, znodePath, firstLockState);

        firstLockGuard.lock();
        firstLockState.await(10, TimeUnit.SECONDS);
        assertThat(firstLockGuard.isLocked()).isTrue();

        firstZookeeperClient.close();
        waitZookeeperClose.await(5, TimeUnit.SECONDS);

        var secondLockState = new LockState(1);
        var secondLockGuard = new LockGuard(this.zookeeperClient, znodePath, secondLockState);

        secondLockGuard.lock();
        secondLockState.await(10, TimeUnit.SECONDS);
        assertThat(secondLockGuard.isLocked()).isTrue();
    }

    private static class LockState implements LockGuardListener {
        final CountDownLatch latch;

        final AtomicBoolean acquired;

        final AtomicBoolean released;

        final AtomicBoolean conflict;

        LockState(int count) {
            this.latch = new CountDownLatch(count);
            this.acquired = new AtomicBoolean(false);
            this.released = new AtomicBoolean(false);
            this.conflict = new AtomicBoolean(false);
        }

        @Override
        public void lockAcquired() {
            this.acquired.set(true);
            this.latch.countDown();
        }

        @Override
        public void lockReleased() {
            this.released.set(true);
            this.latch.countDown();
        }

        @Override
        public void lockConflict() {
            this.conflict.set(true);
            this.latch.countDown();
        }

        void await(long timeout, TimeUnit unit) throws InterruptedException {
            this.latch.await(timeout, unit);
        }
    }

    private static class WaitZookeeperClose implements Watcher {

        private final CountDownLatch latch;

        WaitZookeeperClose() {
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == Watcher.Event.KeeperState.Closed) {
                this.latch.countDown();
            }
        }

        void await(long timeout, TimeUnit unit) throws InterruptedException {
            this.latch.await(timeout, unit);
        }
    }
}
