package com.github.tt4g.zookeeper.playground;

/**
 * {@link AppGuard} event listener.
 */
public interface AppGuardListener {
    /**
     * Called when the application lock is acquired.
     */
    void onStart();

    /**
     * Called when the application lock is released.
     */
    void onFinish();

    /**
     * Called when failing to acquire the application lock.
     */
    void onUnableStart();
}
