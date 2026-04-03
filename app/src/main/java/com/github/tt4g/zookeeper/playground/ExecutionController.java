package com.github.tt4g.zookeeper.playground;

import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import com.github.tt4g.zookeeper.playground.zookeeper.watcher.WatcherChainBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@NullMarked
public class ExecutionController {

    private final Logger logger;

    private final Runner runner;

    private final AppGuard appGuard;

    private ExecutionController(
        Logger logger,
        ZookeeperClient zookeeperClient,
        AppMode appMode
    ) {
        this.logger = logger;
        this.runner = this.createRunner(appMode);
        this.appGuard = this.createAppGuard(zookeeperClient, appMode);
    }

    public static ExecutionController start(
        AppMode appMode,
        ZookeeperConfig zookeeperConfig
    ) throws IOException {
        var logger = LoggerFactory.getLogger(ExecutionController.class);


        var watcherChain = WatcherChainBuilder.create().build();
        var zookeeperListener = new ZookeeperListener(logger);
        var executionWatcher = new ExecutionWatcher(watcherChain, zookeeperListener);

        var zookeeperClient =
            ZookeeperClient.create(
                zookeeperConfig,
                executionWatcher
            );

        var executionController =
            new ExecutionController(
                logger,
                zookeeperClient,
                appMode
            );
        zookeeperListener.setExecutionController(executionController);

        return executionController;
    }

    private Runner createRunner(
        AppMode appMode
    ) {
        return switch (appMode) {
            case AppMode.Producer -> new ProducerRunner();
            case AppMode.Consumer -> new ConsumerRunner();
        };
    }

    private AppGuard createAppGuard(
        ZookeeperClient zookeeperClient,
        AppMode appMode
    ) {
        var appStateListener = new AppStateListener();
        var appPath =
            switch (appMode) {
                case AppMode.Producer -> ZNodePath.root().join("producer");
                case AppMode.Consumer -> ZNodePath.root().join("consumer");
            };
        return AppGuard.create(
            zookeeperClient,
            appPath,
            appStateListener
        );
    }

    void stop() {
        try {
            this.releaseAppGuard();
            this.appGuard.close();
        } catch (Exception ex) {
            this.logger.error("Failed to stop.", ex);
        }
    }

    private void lockAppGuard() {
        try {
            this.appGuard.lock();
        } catch (Exception ex) {
            this.logger.error("Failed to lock AppGuard.", ex);
            System.exit(1);
        }
    }

    private void releaseAppGuard() {
        try {
            this.appGuard.release();
        } catch (Exception ex) {
            this.logger.error("Failed to release AppGuard.", ex);
        }
    }

    private void startRunner() {
        try {
            this.runner.run();
        } catch (Exception ex) {
            this.logger.error("Failed to start runner.", ex);
        }
    }

    private void stopRunner() {
        try {
            this.runner.stop();
        } catch (Exception ex) {
            this.logger.error("Failed to stop runner.", ex);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                () -> {
                    releaseAppGuard();
                    stopRunner();
                },
                this.getClass().getName() + "-shutdown-hook"
            )
        );
    }

    private static class ZookeeperListener implements ZookeeperStateListener {

        private final Logger logger;

        private final ReentrantLock executionControllerLock;

        private final AtomicReference<@Nullable ExecutionController> executionControllerReference;

        private final AtomicBoolean connectionState;

        private ZookeeperListener(
            Logger logger
        ) {
            this.logger = logger;
            this.executionControllerLock = new ReentrantLock();
            this.executionControllerReference = new AtomicReference<>();
            this.connectionState = new AtomicBoolean(false);
        }

        @Override
        public void onConnected(boolean readonly) {
            if (readonly) {
                this.logger.warn("Zookeeper Connected but Read Only Mode.");
                System.exit(1);
            }

            logger.info("Zookeeper Connected.");
            this.connected();
        }

        @Override
        public void onDisconnected() {
            this.logger.warn("Zookeeper Disconnected.");

            this.disconnected();

            System.exit(1);
        }

        @Override
        public void onSaslAuthenticated() {
            this.logger.debug("Zookeeper Authenticated.");
        }

        @Override
        public void onAuthFailed() {
            this.logger.warn("Zookeeper Authentication Failed.");
        }

        @Override
        public void onSessionExpired() {
            this.logger.warn("Zookeeper Session Expired.");
            System.exit(1);
        }

        @Override
        public void onClosed() {
            logger.warn("Zookeeper Connection Closed.");

            this.disconnected();

            System.exit(1);
        }

        void setExecutionController(ExecutionController executionController) {
            this.executionControllerLock.lock();
            try {
                this.executionControllerReference.set(executionController);
                if (this.connectionState.get()) {
                    this.notifyConnected(executionController);
                }

            } finally {
                this.executionControllerLock.unlock();
            }
        }

        private void connected() {
            this.executionControllerLock.lock();
            try {
                var connectedNow =
                    !this.connectionState.compareAndExchange(false, true);
                if (!connectedNow) {
                    return;
                }

                var executionController = this.executionControllerReference.get();
                if (executionController == null) {
                    return;
                }
                this.notifyConnected(executionController);
            } finally {
                this.executionControllerLock.unlock();
            }
        }

        private void notifyConnected(ExecutionController executionController) {
            executionController.lockAppGuard();
            executionController.registerShutdownHook();
        }

        private void disconnected() {
            this.executionControllerLock.lock();
            try {
                var diconnectedNow =
                    this.connectionState.compareAndExchange(true, false);
                if (!diconnectedNow) {
                    return;
                }

                var executionController = this.executionControllerReference.get();
                if (executionController == null) {
                    return;
                }
                this.notifyDisconnected(executionController);
            } finally {
                this.executionControllerLock.unlock();
            }
        }

        private void notifyDisconnected(ExecutionController executionController) {
            executionController.stopRunner();
        }
    }

    private class AppStateListener implements AppGuardListener {

        @Override
        public void onStart() {
            startRunner();
        }

        @Override
        public void onFinish() {
            stopRunner();
        }

        @Override
        public void onUnableStart() {
            stop();
        }
    }
}
