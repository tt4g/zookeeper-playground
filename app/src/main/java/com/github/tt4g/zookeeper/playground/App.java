package com.github.tt4g.zookeeper.playground;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class App {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    static void main(String[] args) {
        new App().run();
    }

    public void run() {
        try {
            this.logger.atInfo().log("Start.");

            var dotenv = this.loadDotenv();
            var zookeeperConfig = ZookeeperConfig.load(dotenv);
            ExecutionController.start(zookeeperConfig);

        } catch (Exception ex) {
            this.logger.atError().addArgument(ex).log("An error occurred!");
        } finally {
            this.logger.atInfo().log("Complete.");
        }
    }

    private Dotenv loadDotenv() {
        var currentDirectory = Paths.get(".").toAbsolutePath();
        return Dotenv.configure()
            .directory(currentDirectory.toString())
            .filename("zookeeper_playground.env")
            .load();
    }
}
