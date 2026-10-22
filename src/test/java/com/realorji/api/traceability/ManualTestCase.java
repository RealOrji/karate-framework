package com.realorji.api.traceability;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** One row of test-cases/manual-test-cases.csv - the manual test case register. */
public record ManualTestCase(
        String id,
        String module,
        String title,
        String priority,
        String type,
        String automationStatus,
        String automatedIn) {

    public static final String AUTOMATED = "Automated";
    public static final String MANUAL = "Manual";

    public boolean isAutomated() {
        return AUTOMATED.equalsIgnoreCase(automationStatus);
    }

    public static List<ManualTestCase> readAll(Path csv) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .toList();

        List<String> header = parseLine(lines.get(0));
        Map<String, Integer> column = IntStream.range(0, header.size()).boxed()
                .collect(Collectors.toMap(header::get, Function.identity()));

        List<ManualTestCase> cases = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> cells = parseLine(lines.get(i));
            Function<String, String> cell = name -> cells.get(column.get(name)).trim();
            cases.add(new ManualTestCase(
                    cell.apply("test_case_id"),
                    cell.apply("module"),
                    cell.apply("title"),
                    cell.apply("priority"),
                    cell.apply("type"),
                    cell.apply("automation_status"),
                    cell.apply("automated_in")));
        }
        return cases;
    }

    /** Minimal RFC 4180 parser: quoted fields may contain commas and doubled quotes. */
    static List<String> parseLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }
}
