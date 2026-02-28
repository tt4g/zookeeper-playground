package com.github.tt4g.zookeeper.playground.zookeeper.lock;

import com.github.tt4g.zookeeper.playground.zookeeper.AbstractZookeeperTest;
import com.github.tt4g.zookeeper.playground.zookeeper.client.ZookeeperClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    void testIsLocked() {
        // TODO
    }
}
