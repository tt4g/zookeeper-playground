package com.github.tt4g.zookeeper.playground;

public interface ZookeeperStateListener {
    /**
     * Called when zookeeper connected.
     *
     * @param readonly `true` if connected to a readonly zookeeper server,
     *                 otherwise `false`.
     */
    void onConnected(boolean readonly);

    /**
     * Called when zookeeper disconnected.
     */
    void onDisconnected();

    /**
     * Called when SASL authentication success.
     */
    void onSaslAuthenticated();

    /**
     * Called when authentication failure.
     */
    void onAuthFailed();

    /**
     * Called when zookeeper session expired.
     */
    void onSessionExpired();

    /**
     * Called when zookeeper connection closed.
     */
    void onClosed();
}
