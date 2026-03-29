package com.github.tt4g.zookeeper.playground;

import io.github.cdimascio.dotenv.Dotenv;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record ZookeeperConfig(
    @NonNull String zookeeperHost,
    int sessionTimeout
) {

    public static ZookeeperConfig load(Dotenv dotenv) {
        String zookeeperHost =
            Objects.requireNonNull(
                dotenv.get("ZOOKEEPER_PLAYGROUND_ZOOKEEPER_HOST")
            );

        // session timeout (milliseconds).
        String sessionTimeout =
            Objects.requireNonNull(
                dotenv.get("ZOOKEEPER_PLAYGROUND_ZOOKEEPER_SESSION_TIMEOUT")
            );
        var timeoutMilliseconds = Integer.parseInt(sessionTimeout);

        return new ZookeeperConfig(zookeeperHost, timeoutMilliseconds);
    }
}
