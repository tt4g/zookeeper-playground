package com.github.tt4g.zookeeper.playground.zookeeper.queue;

import org.jspecify.annotations.NullMarked;

/**
 * The lease value retrieved from the queue.
 *
 * @param <E> An element type.
 */
@NullMarked
public interface LeaseElement<E> {
    /**
     * @return An element.
     */
    E value();

    /**
     * Delete the leased value from the Zookeeper server.
     *
     * @throws Exception Thrown when the value could not be deleted from the
     *                   Zookeeper server.
     */
    void ack() throws Exception;

    /**
     * Put back the leased value.
     *
     * @throws Exception Thrown when the value could not be returned.
     */
    void putBack() throws Exception;

    /**
     * @return `true` if this element is leased, otherwise returns `false`.
     */
    boolean isLeased();
}
