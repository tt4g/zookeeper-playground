package com.github.tt4g.zookeeper.playground;

import org.jspecify.annotations.NullMarked;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

@NullMarked
public class AppArgs {
    @CommandLine.Option(
        names = "--mode",
        required = true,
        description = "Candidates: ${COMPLETION-CANDIDATES}",
        converter = AppModeTypeConverter.class,
        completionCandidates = AppModeCandidate.class
    )
    private AppMode appMode;

    public AppMode getAppMode() {
        return this.appMode;
    }

    private static class AppModeTypeConverter implements CommandLine.ITypeConverter<AppMode> {

        @Override
        public AppMode convert(String value) throws Exception {
            return switch (value) {
                case "producer" -> AppMode.Producer;
                case "consumer" -> AppMode.Consumer;
                default -> {
                    throw new CommandLine.TypeConversionException(
                        "Invalid value " +
                            value + ": must be one of " +
                            Arrays.toString(AppMode.values())
                    );
                }
            };
        }
    }

    private static class AppModeCandidate implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Arrays.stream(AppMode.values())
                .map(appMode -> appMode.toString().toLowerCase(Locale.ROOT))
                .iterator();
        }
    }
}
