package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import com.github.tt4g.zookeeper.playground.zookeeper.lock.LockGuard;
import com.github.tt4g.zookeeper.playground.zookeeper.lock.LockGuardListener;
import org.apache.zookeeper.KeeperException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class AppGuard implements AutoCloseable {
    private final Logger logger;

    private final LockGuard lockGuard;

    private AppGuard(
        Logger logger,
        LockGuard lockGuard
    ) {
        this.logger = logger;
        this.lockGuard = lockGuard;
    }

    static AppGuard create(
        ZookeeperClient zookeeperClient,
        ZNodePath appPath,
        AppGuardListener appGuardListener
    ) {
        var logger = LoggerFactory.getLogger(AppGuard.class);
        var appGuardListenerAdapter =
            new AppGuardListenerAdapter(logger, appGuardListener);
        var lockGuard =
            new LockGuard(
                zookeeperClient,
                appPath,
                appGuardListenerAdapter
            );

        return new AppGuard(logger, lockGuard);
    }

    void lock() throws InterruptedException, KeeperException {
        this.lockGuard.lock();
    }

    void release() throws InterruptedException, KeeperException {
        this.lockGuard.unlock();
    }

    @Override
    public void close() throws Exception {
        this.lockGuard.close();
    }

    private record AppGuardListenerAdapter(
        Logger logger,
        AppGuardListener appGuardListener
    ) implements LockGuardListener {

        @Override
        public void lockAcquired() {
            this.logger.info("AppGuard acquired.");
            this.appGuardListener.onStart();
        }

        @Override
        public void lockReleased() {
            this.logger.info("AppGuard released.");
            this.appGuardListener.onFinish();
        }

        @Override
        public void lockConflict() {
            this.logger.error("AppGuard conflict.");
            this.appGuardListener.onUnableStart();
        }
    }
}
