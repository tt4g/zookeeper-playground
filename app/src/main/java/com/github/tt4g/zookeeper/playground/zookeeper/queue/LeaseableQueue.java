package com.github.tt4g.zookeeper.playground.zookeeper.queue;

import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * A queue that supports lease operations.
 *
 * @param <E> Element type.
 */
@NullMarked
public interface LeaseableQueue<E> {
    /**
     * Lease the head element.
     *
     * @return {@link LeaseElement} if a value is retrieved from the Zookeeper
     * server, otherwise returns {@link Optional#empty()}.
     * @throws Exception Thrown when a value could not be retrieved from the
     *                   Zookeeper server.
     */
    Optional<LeaseElement<E>> lease() throws Exception;
}
