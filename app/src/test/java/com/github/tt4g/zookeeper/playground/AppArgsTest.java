package com.github.tt4g.zookeeper.playground;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppArgsTest {
    @Test
    void testParseEmptyArgs() {
        assertThatThrownBy(() -> {
            String[] args = new String[]{};
            var appArgs = new AppArgs();
            new CommandLine(appArgs).parseArgs(args);
        })
            .isInstanceOf(CommandLine.MissingParameterException.class)
            .hasMessage("Missing required option: '--mode=<appMode>'");
    }

    @Test
    void testParseModeOptionArg() {
        String[] producerOptionArgs = new String[]{"--mode", "producer"};
        var producerAppArgs = new AppArgs();
        new CommandLine(producerAppArgs).parseArgs(producerOptionArgs);
        assertThat(producerAppArgs.getAppMode()).isEqualTo(AppMode.Producer);

        String[] consumerOptionArgs = new String[]{"--mode", "consumer"};
        var consumerAppArgs = new AppArgs();
        new CommandLine(consumerAppArgs).parseArgs(consumerOptionArgs);
        assertThat(consumerAppArgs.getAppMode()).isEqualTo(AppMode.Consumer);
    }

    @Test
    void testUsage() {
        var outWriter = new StringWriter();
        new CommandLine(new AppArgs()).usage(new PrintWriter(outWriter));
        var out = outWriter.toString();
        assertThat(out).isEqualToNormalizingWhitespace(
            "Usage: <main class> --mode=<appMode>\n"
                + "      --mode=<appMode>   Candidates: producer, consumer\n"
        );
    }
}
