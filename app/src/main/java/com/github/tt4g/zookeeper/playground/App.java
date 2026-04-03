package com.github.tt4g.zookeeper.playground;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Paths;

public class App {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    static void main(String[] args) {
        var appArgs = new AppArgs();
        new CommandLine(appArgs).parseArgs(args);

        new App().run(appArgs);
    }

    public void run(AppArgs appArgs) {
        try {
            this.logger.info("Start.");

            var dotenv = this.loadDotenv();
            var zookeeperConfig = ZookeeperConfig.load(dotenv);
            ExecutionController.start(
                appArgs.getAppMode(),
                zookeeperConfig
            );

        } catch (Exception ex) {
            this.logger.error("An error occurred!", ex);
        } finally {
            this.logger.info("Complete.");
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
