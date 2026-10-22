package com.realorji.api.support;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Turns Karate's Cucumber JSON output into the masterthought HTML report
 * (target/cucumber-html-reports/overview-features.html).
 */
public final class CucumberReport {

    private static final String PROJECT_NAME = "Karate API Automation Framework";

    private CucumberReport() {
    }

    public static void generate(String karateReportDir) {
        List<String> jsonFiles = findCucumberJson(Path.of(karateReportDir));
        if (jsonFiles.isEmpty()) {
            return;
        }

        Configuration config = new Configuration(new File("target"), PROJECT_NAME);
        config.addClassifications("Environment", System.getProperty("karate.env", "qa"));
        config.addClassifications("Threads", System.getProperty("threads", "5"));
        config.addClassifications("Java", System.getProperty("java.version"));

        new ReportBuilder(jsonFiles, config).generateReports();
    }

    private static List<String> findCucumberJson(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".json"))
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
