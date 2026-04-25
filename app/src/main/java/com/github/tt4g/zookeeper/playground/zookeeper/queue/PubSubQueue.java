package com.github.tt4g.zookeeper.playground.zookeeper.queue;

import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pub/Sub queue.
 */
@NullMarked
public class PubSubQueue implements LeaseableQueue<String>, PushableQueue<String> {

    private final Logger logger = LoggerFactory.getLogger(PubSubQueue.class);

    private final String queuePrefix = "queue-";

    private final String pendingPrefix = "pending-";

    private final ZookeeperClient zookeeperClient;

    private final ZNodePath queueRootPath;

    private final List<ACL> acl;

    public PubSubQueue(
        ZookeeperClient zookeeperClient,
        ZNodePath queueRootPath,
        List<ACL> acl
    ) {
        this.zookeeperClient = zookeeperClient;
        this.queueRootPath = queueRootPath;
        this.acl = acl;
    }

    public PubSubQueue(
        ZookeeperClient zookeeperClient,
        ZNodePath queueRootPath
    ) {
        this(zookeeperClient, queueRootPath, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    @Override
    public void push(String element) throws Exception {
        this.createQueueNode(element);
    }

    @Override
    public Optional<LeaseElement<String>> lease() throws Exception {
        var sequenceToChildNodePath = this.collectOrderedChildren();
        if (sequenceToChildNodePath.isEmpty()) {
            return Optional.empty();
        }

        for (var childNodePath : sequenceToChildNodePath.values()) {
            var lease = this.tryLease(childNodePath);
            if (lease.isPresent()) {
                return Optional.of(lease.get());
            }
        }

        return Optional.empty();
    }

    private void createQueueNode(String element) throws InterruptedException, KeeperException {
        this.createQueueNode(element, 2);
    }

    private void createQueueNode(String element, int maxTry) throws InterruptedException, KeeperException {
        var queueNodePath = this.queueRootPath.join(this.queuePrefix);
        var data = element.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < maxTry; ++i) {
            try {
                this.zookeeperClient.createPersistentSequential(queueNodePath, data, this.acl);

                break;
            } catch (KeeperException.NoNodeException ex) {
                // Create the queue root node if it does not exist.
                this.createQueueRootNode();
            }
        }
    }

    private void createQueueRootNode() throws InterruptedException, KeeperException {
        this.logger.debug(
            "Create missing Queue Root Node \"{}\"",
            this.queueRootPath
        );

        this.zookeeperClient.createPersistent(this.queueRootPath, new byte[0], this.acl);
    }

    private Optional<String> readElementData(ChildNodePath childNodePath) throws InterruptedException {
        var childPath = childNodePath.nodePath();
        try {
            var data = this.zookeeperClient.getData(childPath);

            return Optional.of(
                new String(data, StandardCharsets.UTF_8)
            );
        } catch (KeeperException ex) {
            this.logger.warn(
                "Failed to get data from child znode: \"{}\"",
                childPath,
                ex
            );

            return Optional.empty();
        }
    }

    private Map<Long, ChildNodePath> collectOrderedChildren() throws InterruptedException, KeeperException {
        Map<Long, ChildNodePath> sequenceToChildren = new TreeMap<>();

        List<String> childCandidateNames = this.collectElementCandidateNames();
        for (String childCandidateName : childCandidateNames) {
            if (childCandidateName.startsWith(this.pendingPrefix)) {
                // Skip pending element nodes.
                continue;
            }
            
            var childSequence = this.parseQueueNodeName(childCandidateName);
            if (childSequence.isEmpty()) {
                this.logger.warn(
                    "Found invalid child node (invalid znode name): \"{}\"",
                    childCandidateName
                );

                continue;
            }

            var sequence = childSequence.getAsLong();
            var childPath = this.queueRootPath.join(childCandidateName);
            var childNodePath = new ChildNodePath(sequence, childPath);
            sequenceToChildren.put(sequence, childNodePath);
        }

        return sequenceToChildren;
    }

    private List<String> collectElementCandidateNames() throws InterruptedException, KeeperException {
        return this.zookeeperClient.getChildren(this.queueRootPath);
    }

    private OptionalLong parseQueueNodeName(String znodeName) {
        if (!znodeName.startsWith(this.queuePrefix)) {
            return OptionalLong.empty();
        }

        String sequence = znodeName.substring(this.queuePrefix.length());
        try {
            return OptionalLong.of(Long.parseLong(sequence));
        } catch (NumberFormatException _) {
            return OptionalLong.empty();
        }
    }

    private Optional<Lease> tryLease(ChildNodePath childNodePath) throws InterruptedException, KeeperException {
        var pendingPath = this.cratePendingPath(childNodePath);
        var pending = this.tryPending(pendingPath);
        if (!pending) {
            return Optional.empty();
        }

        var elementData = this.readElementData(childNodePath);

        return elementData.map(data ->
            new Lease(
                this.logger,
                this.zookeeperClient,
                data,
                childNodePath,
                pendingPath
            )
        );
    }

    private ZNodePath cratePendingPath(ChildNodePath childNodePath) {
        return this.queueRootPath.join(this.pendingPrefix + childNodePath.sequence());
    }

    private boolean tryPending(ZNodePath pendingPath) throws InterruptedException, KeeperException {
        try {
            this.zookeeperClient.createEphemeral(pendingPath, new byte[0], this.acl);

            return true;

        } catch (KeeperException ex) {
            if (ex.code() == KeeperException.Code.NODEEXISTS) {
                this.logger.debug("Pending Node already exists: \"{}\"", pendingPath, ex);

                return false;
            }

            throw ex;
        }
    }

    private record ChildNodePath(
        long sequence,
        ZNodePath nodePath
    ) {
    }

    private static class Lease implements LeaseElement<String> {
        private final AtomicBoolean leased;

        private final Logger logger;

        private final ZookeeperClient zookeeperClient;

        private final String data;

        private final ChildNodePath childNodePath;

        private final ZNodePath pendingPath;

        private Lease(
            Logger logger,
            ZookeeperClient zookeeperClient,
            String data,
            ChildNodePath childNodePath,
            ZNodePath pendingPath
        ) {
            this.leased = new AtomicBoolean(true);
            this.logger = logger;
            this.zookeeperClient = zookeeperClient;
            this.data = data;
            this.childNodePath = childNodePath;
            this.pendingPath = pendingPath;
        }


        @Override
        public String value() {
            if (!this.leased.get()) {
                throw new IllegalStateException("Invalid Element");
            }

            return this.data;
        }

        @Override
        public void ack() throws Exception {
            if (!this.changeStateToPutBack()) {
                throw new IllegalStateException("Unable ACK (invalid element)");
            }

            var childPath = this.childNodePath.nodePath();

            try {
                this.zookeeperClient.forceDelete(childPath);

                this.logger.warn(
                    "Delete the element znode: \"{}\"", childPath
                );
            } catch (KeeperException.NoNodeException ex) {
                this.logger.warn(
                    "The element znode was already deleted: \"{}\"",
                    childPath,
                    ex
                );
            } catch (KeeperException ex) {
                this.logger.warn(
                    "Failed to delete the element znode: \"{}\"",
                    childPath,
                    ex
                );

                throw ex;
            }
        }

        @Override
        public void putBack() throws Exception {
            if (!this.changeStateToPutBack()) {
                throw new IllegalStateException("Unable put back (invalid element)");
            }

            try {
                this.zookeeperClient.forceDelete(this.pendingPath);

                this.logger.warn(
                    "Delete the pending znode: \"{}\"",
                    this.pendingPath
                );
            } catch (KeeperException.NoNodeException ex) {
                this.logger.warn(
                    "The pending znode was already deleted: \"{}\"",
                    this.pendingPath,
                    ex
                );
            } catch (KeeperException ex) {
                this.logger.warn(
                    "Failed to delete the pending znode: \"{}\"",
                    this.pendingPath,
                    ex
                );

                throw ex;
            }
        }

        @Override
        public boolean isLeased() {
            return this.leased.get();
        }

        /**
         * @return `true` if state changed, otherwise returns `false`.
         */
        private boolean changeStateToPutBack() {
            return this.leased.compareAndSet(true, false);
        }
    }
}
