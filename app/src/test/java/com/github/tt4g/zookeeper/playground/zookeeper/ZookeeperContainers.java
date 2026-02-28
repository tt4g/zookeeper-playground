package com.github.tt4g.zookeeper.playground.zookeeper;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
public class ZookeeperContainers implements AutoCloseable {
    private final ComposeContainer compose;

    @Nullable
    private String connectionString;

    public ZookeeperContainers() {
        var classLoader = this.getClass().getClassLoader();
        var composeFile = new File(classLoader.getResource("compose-test.yml").getFile());

        this.compose =
            new ComposeContainer(
                DockerImageName.parse("docker:29.2.1"),
                composeFile
            )
                .withExposedService(
                    "zookeeper_playground_zookeeper_test_1",
                    2181,
                    Wait.forListeningPort()
                )
                .withExposedService(
                    "zookeeper_playground_zookeeper_test_2",
                    2181,
                    Wait.forListeningPort()
                )
                .withExposedService(
                    "zookeeper_playground_zookeeper_test_3",
                    2181,
                    Wait.forListeningPort()
                );

        this.connectionString = null;
    }

    public void start() {
        this.compose.start();
    }

    @Override
    public void close() {
        this.compose.close();
    }


    public String getZookeeperConnectionString() {
        if (this.connectionString != null) {
            return this.connectionString;
        }

        // Generate Zookeeper connection string:
        // `"{host1}:{port1},{host2}:{port2}, ..."`
        this.connectionString =
            List.of(
                    "zookeeper_playground_zookeeper_test_1",
                    "zookeeper_playground_zookeeper_test_2",
                    "zookeeper_playground_zookeeper_test_3"
                )
                .stream()
                .map(serviceName -> {
                    var host = this.compose.getServiceHost(serviceName, 2181);
                    var port = this.compose.getServicePort(serviceName, 2181);
                    return host + ":" + port;
                })
                .collect(Collectors.joining(","));

        return this.connectionString;
    }
}
