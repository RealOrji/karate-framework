package com.realorji.api;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.realorji.api.support.CucumberReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Single entry point for CI: runs every feature under classpath:features in parallel.
 *
 * <pre>
 *   ./mvnw test                                          # full regression (qa)
 *   ./mvnw test -Dkarate.options="--tags @smoke"         # smoke only
 *   ./mvnw test -Dkarate.options="--tags @TC-BKG-001"    # one manual test case
 *   ./mvnw test -Dkarate.env=staging -Dthreads=8
 * </pre>
 *
 * Tags passed through -Dkarate.options replace the defaults below.
 */
class ApiTestSuite {

    @Test
    void runAllFeaturesInParallel() {
        int threads = Integer.getInteger("threads", 5);

        Results results = Runner.path("classpath:features")
                .tags("~@ignore", "~@wip")
                .outputCucumberJson(true)
                .outputJunitXml(true)
                .parallel(threads);

        CucumberReport.generate(results.getReportDir());

        assertTrue(results.getScenariosTotal() > 0,
                "No scenarios matched the tag selection - check -Dkarate.options for typos");
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
