package com.github.tt4g.zookeeper.playground.zookeeper.queue;

import org.jspecify.annotations.NullMarked;

/**
 * A queue that supports push operations.
 *
 * @param <E> An element type.
 */
@NullMarked
public interface PushableQueue<E> {
    /**
     * Push an element to this queue.
     *
     * @param element An element.
     * @throws Exception Thrown when a value could not be added from the
     *                   Zookeeper server.
     */
    void push(E element) throws Exception;
}
