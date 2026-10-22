package features.placeholder;

import com.intuit.karate.junit5.Karate;

/** IDE convenience runner for the JSONPlaceholder features. CI uses ApiTestSuite instead. */
class PlaceholderRunner {

    @Karate.Test
    Karate placeholder() {
        return Karate.run().relativeTo(getClass());
    }
}
