package com.realorji.api.support;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thread-safe test data generators, exposed to features as {@code DataFactory}
 * through karate-config.js.
 *
 * <pre>
 *   * def lastname = DataFactory.uniqueName('Guest')
 *   * def checkin  = DataFactory.dateFromToday(7)
 * </pre>
 */
public final class DataFactory {

    private static final List<String> FIRST_NAMES =
            List.of("Ada", "Grace", "Alan", "Linus", "Margaret", "Dennis", "Barbara", "Ken");
    private static final List<String> EXTRAS =
            List.of("Breakfast", "Late checkout", "Airport pickup", "Extra pillows", "Parking");
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private DataFactory() {
    }

    public static String firstName() {
        return pick(FIRST_NAMES);
    }

    public static String additionalNeeds() {
        return pick(EXTRAS);
    }

    /** A name that is unique per run, so searches on shared environments only find our own data. */
    public static String uniqueName(String prefix) {
        return prefix + randomLetters(8);
    }

    public static int price(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** ISO-8601 date (yyyy-MM-dd) offset from today; negative values go into the past. */
    public static String dateFromToday(int days) {
        return LocalDate.now().plusDays(days).toString();
    }

    public static String randomLetters(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(LETTERS.charAt(ThreadLocalRandom.current().nextInt(LETTERS.length())));
        }
        return sb.toString();
    }

    private static String pick(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
}
