package com.realorji.api.traceability;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the manual test case register (test-cases/manual-test-cases.csv) and the
 * Karate features in sync. Runs offline in milliseconds, so it gates every build.
 *
 * It also writes target/traceability-report.md, which CI appends to the job summary.
 */
@DisplayName("Manual test case <-> automation traceability")
class TraceabilityTest {

    private static final Path REGISTER = Path.of("test-cases", "manual-test-cases.csv");
    private static final Path FEATURE_ROOT = Path.of("src", "test", "java");
    private static final Path REPORT = Path.of("target", "traceability-report.md");

    private static List<ManualTestCase> register;
    private static Map<String, ManualTestCase> registerById;
    private static List<FeatureScenario> runnableScenarios;
    private static Map<String, List<FeatureScenario>> scenariosById;

    @BeforeAll
    static void load() throws IOException {
        register = ManualTestCase.readAll(REGISTER);
        registerById = register.stream()
                .collect(Collectors.toMap(ManualTestCase::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        runnableScenarios = FeatureScenario.scan(FEATURE_ROOT).stream()
                .filter(FeatureScenario::isRunnable)
                .toList();
        scenariosById = new LinkedHashMap<>();
        for (FeatureScenario scenario : runnableScenarios) {
            for (String id : scenario.testCaseIds()) {
                scenariosById.computeIfAbsent(id, k -> new ArrayList<>()).add(scenario);
            }
        }
    }

    @Test
    @DisplayName("Test case IDs in the register are unique")
    void registerIdsAreUnique() {
        List<String> duplicates = register.stream()
                .collect(Collectors.groupingBy(ManualTestCase::id, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        assertTrue(duplicates.isEmpty(), "Duplicate IDs in " + REGISTER + ": " + duplicates);
    }

    @Test
    @DisplayName("Every runnable scenario is mapped to exactly one test case")
    void everyScenarioHasExactlyOneTestCaseTag() {
        List<String> problems = runnableScenarios.stream()
                .filter(s -> s.testCaseIds().size() != 1)
                .map(s -> s.location() + " '" + s.name() + "' has " + s.testCaseIds())
                .toList();
        assertTrue(problems.isEmpty(), "Scenarios need exactly one @TC-* tag:\n" + String.join("\n", problems));
    }

    @Test
    @DisplayName("Every @TC tag in a feature exists in the register")
    void everyTagIsRegistered() {
        List<String> unknown = scenariosById.entrySet().stream()
                .filter(e -> !registerById.containsKey(e.getKey()))
                .map(e -> e.getKey() + " at " + e.getValue().get(0).location())
                .toList();
        assertTrue(unknown.isEmpty(), "Add these to " + REGISTER + ":\n" + String.join("\n", unknown));
    }

    @Test
    @DisplayName("A test case is automated by one scenario only")
    void noTestCaseIsAutomatedTwice() {
        List<String> duplicated = scenariosById.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey() + " -> " + e.getValue().stream().map(FeatureScenario::location).toList())
                .toList();
        assertTrue(duplicated.isEmpty(), "Test case tags reused:\n" + String.join("\n", duplicated));
    }

    @Test
    @DisplayName("Register status matches the code (Automated <-> tagged scenario in the named feature)")
    void automationStatusMatchesCode() {
        List<String> problems = new ArrayList<>();
        for (ManualTestCase tc : register) {
            List<FeatureScenario> scenarios = scenariosById.getOrDefault(tc.id(), List.of());
            if (tc.isAutomated() && scenarios.isEmpty()) {
                problems.add(tc.id() + " is marked Automated but no scenario carries @" + tc.id());
            } else if (tc.isAutomated() && !scenarios.get(0).file().equals(tc.automatedIn())) {
                problems.add(tc.id() + " register says " + tc.automatedIn() + " but it lives in " + scenarios.get(0).file());
            } else if (!tc.isAutomated() && !scenarios.isEmpty()) {
                problems.add(tc.id() + " is marked " + tc.automationStatus() + " but is automated at "
                        + scenarios.get(0).location() + " - set it to Automated");
            } else if (!Set.of(ManualTestCase.AUTOMATED, ManualTestCase.MANUAL).contains(tc.automationStatus())) {
                problems.add(tc.id() + " has unknown automation_status '" + tc.automationStatus() + "'");
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @AfterAll
    static void writeReport() throws IOException {
        long automated = register.stream().filter(ManualTestCase::isAutomated).count();
        double coverage = register.isEmpty() ? 0 : 100.0 * automated / register.size();

        StringBuilder md = new StringBuilder()
                .append("## Test case traceability\n\n")
                .append(String.format("**%d / %d** registered test cases automated (**%.1f%%**).%n%n",
                        automated, register.size(), coverage))
                .append("| ID | Module | Title | Priority | Status | Scenario |\n")
                .append("|---|---|---|---|---|---|\n");

        for (ManualTestCase tc : register) {
            List<FeatureScenario> scenarios = scenariosById.getOrDefault(tc.id(), List.of());
            String where = scenarios.isEmpty() ? "-" : "`" + scenarios.get(0).location() + "`";
            md.append(String.format("| %s | %s | %s | %s | %s | %s |%n",
                    tc.id(), tc.module(), tc.title(), tc.priority(), tc.automationStatus(), where));
        }

        Files.createDirectories(REPORT.getParent());
        Files.writeString(REPORT, md.toString(), StandardCharsets.UTF_8);
    }
}
