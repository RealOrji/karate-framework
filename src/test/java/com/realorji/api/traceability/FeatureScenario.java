package com.realorji.api.traceability;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** A Scenario / Scenario Outline found in a .feature file, with its effective tags. */
public record FeatureScenario(String file, int line, String name, Set<String> tags) {

    public static final String TEST_CASE_TAG_PREFIX = "@TC-";

    public List<String> testCaseIds() {
        return tags.stream()
                .filter(tag -> tag.startsWith(TEST_CASE_TAG_PREFIX))
                .map(tag -> tag.substring(1))
                .toList();
    }

    public boolean isRunnable() {
        return !tags.contains("@ignore");
    }

    public String location() {
        return file + ":" + line;
    }

    /** Scans every .feature file below {@code root}; file paths are reported relative to it. */
    public static List<FeatureScenario> scan(Path root) throws IOException {
        List<FeatureScenario> scenarios = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".feature"))
                    .sorted()
                    .forEach(p -> scenarios.addAll(parse(root, p)));
        }
        return scenarios;
    }

    private static List<FeatureScenario> parse(Path root, Path featureFile) {
        String relative = root.relativize(featureFile).toString().replace('\\', '/');
        List<String> lines;
        try {
            lines = Files.readAllLines(featureFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<FeatureScenario> scenarios = new ArrayList<>();
        Set<String> featureTags = new LinkedHashSet<>();
        Set<String> pendingTags = new LinkedHashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).trim();
            if (text.startsWith("@")) {
                pendingTags.addAll(tagsOn(text));
            } else if (text.startsWith("Feature:")) {
                featureTags.addAll(pendingTags);
                pendingTags.clear();
            } else if (text.startsWith("Scenario:") || text.startsWith("Scenario Outline:")) {
                Set<String> tags = new LinkedHashSet<>(featureTags);
                tags.addAll(pendingTags);
                pendingTags.clear();
                String name = text.substring(text.indexOf(':') + 1).trim();
                scenarios.add(new FeatureScenario(relative, i + 1, name, tags));
            } else if (text.startsWith("Examples:") && !scenarios.isEmpty()) {
                scenarios.get(scenarios.size() - 1).tags().addAll(pendingTags);
                pendingTags.clear();
            } else if (text.startsWith("Background:")) {
                pendingTags.clear();
            }
        }
        return scenarios;
    }

    private static List<String> tagsOn(String line) {
        List<String> tags = new ArrayList<>();
        for (String token : line.split("\\s+")) {
            if (token.startsWith("#")) {
                break;
            }
            if (token.startsWith("@")) {
                tags.add(token);
            }
        }
        return tags;
    }
}
