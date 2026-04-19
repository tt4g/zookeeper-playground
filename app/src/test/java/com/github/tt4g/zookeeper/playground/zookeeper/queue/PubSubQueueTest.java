package com.github.tt4g.zookeeper.playground.zookeeper.queue;

import com.github.tt4g.zookeeper.playground.zookeeper.AbstractZookeeperTest;
import com.github.tt4g.zookeeper.playground.zookeeper.ZNodePath;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PubSubQueueTest extends AbstractZookeeperTest {
    private ZNodePath testNodePath;

    private ZookeeperClient zookeeperClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException, KeeperException {
        this.testNodePath = ZNodePath.root().join("PubSubQueueTest");
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

    private PubSubQueue createQueue(ZookeeperClient zookeeperClient) {
        return new PubSubQueue(
            zookeeperClient,
            this.testNodePath
        );
    }

    private PubSubQueue createQueue() {
        return this.createQueue(this.zookeeperClient);
    }

    @Test
    void testLeaseAfterPush() throws Exception {
        var pubSubQueue = createQueue();
        pubSubQueue.push("test-element");
        assertThat(pubSubQueue.lease()).hasValueSatisfying(leaseElement -> {
            assertThat(leaseElement.value()).isEqualTo("test-element");
            assertThat(leaseElement.isLeased()).isTrue();

            assertThatNoException().isThrownBy(() -> {
                leaseElement.ack();
            });

            assertThat(leaseElement.isLeased()).isFalse();
        });
    }

    @Test
    void testLeaseWhenNoElement() throws Exception {
        var pubSubQueue = createQueue();
        assertThat(pubSubQueue.lease()).isEmpty();
    }

    @Test
    void testLeaseAfterPushFromAnotherQueue() throws Exception {
        var pubSubQueue = createQueue();

        var anotherZookeeperClient = this.createZookeeperClient();
        var anotherPubSubQueue = createQueue(anotherZookeeperClient);
        anotherPubSubQueue.push("test-element");

        assertThat(pubSubQueue.lease()).hasValueSatisfying(leaseElement -> {
            assertThat(leaseElement.value()).isEqualTo("test-element");

            assertThatNoException().isThrownBy(() -> {
                leaseElement.ack();
            });
        });
    }

    @Test
    void testLeaseTwiceAndPushOnce() throws Exception {
        var pubSubQueue = createQueue();

        var anotherZookeeperClient = this.createZookeeperClient();
        var anotherPubSubQueue = createQueue(anotherZookeeperClient);

        pubSubQueue.push("test-element");
        assertThat(pubSubQueue.lease()).hasValueSatisfying(leaseElement -> {
            assertThat(leaseElement.value()).isEqualTo("test-element");

            assertThatNoException().isThrownBy(() -> {
                assertThat(anotherPubSubQueue.lease()).isEmpty();
                leaseElement.ack();
            });
        });
    }

    @Test
    void testPutBack() throws Exception {
        var pubSubQueue = createQueue();

        var anotherZookeeperClient = this.createZookeeperClient();
        var anotherPubSubQueue = createQueue(anotherZookeeperClient);

        pubSubQueue.push("test-element");
        assertThat(pubSubQueue.lease()).hasValueSatisfying(leaseElement -> {
            assertThat(leaseElement.value()).isEqualTo("test-element");

            assertThatNoException().isThrownBy(() -> {
                assertThat(anotherPubSubQueue.lease()).isEmpty();
                leaseElement.putBack();
            });
        });

        assertThat(anotherPubSubQueue.lease()).hasValueSatisfying(leaseElement -> {
            assertThatNoException().isThrownBy(() -> {
                leaseElement.ack();
            });
        });
    }
}
