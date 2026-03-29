package com.github.tt4g.zookeeper.playground.zookeeper.lock;

public interface LockGuardListener {
    /**
     * Called when the lock is acquired.
     */
    void lockAcquired();

    /**
     * Called when the lock is released.
     */
    void lockReleased();

    /**
     * Called when the lock is conflict.
     */
    void lockConflict();
}
