package com.github.tt4g.zookeeper.playground.zookeeper.lock;

import com.github.tt4g.zookeeper.playground.zookeeper.AbstractZookeeperTest;
import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
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

    private ZookeeperClient zookeeperClient;

    @BeforeEach
    void setUp() throws IOException {
        this.zookeeperClient = this.createZookeeperClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.zookeeperClient.close();
    }

    @Test
    @Timeout(15)
    void testLock() throws InterruptedException, KeeperException {
        var znodePath = ZNodePath.root().join("lock");
        var lockState = new LockState(1);
        var lockGuard = new LockGuard(this.zookeeperClient, znodePath, lockState);

        lockGuard.lock();
        lockState.latch.await(10, TimeUnit.SECONDS);

        assertThat(lockGuard.isLocked()).isTrue();
        assertThat(lockState.acquired).isTrue();
        assertThat(lockState.released).isFalse();
        assertThat(lockState.conflict).isFalse();
    }

    @Test
    @Timeout(15)
    void testUnlock() {
        // TODO
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
    }
}
