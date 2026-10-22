package features.booker;

import com.intuit.karate.junit5.Karate;

/** IDE convenience runner for the Restful-Booker features. CI uses ApiTestSuite instead. */
class BookerRunner {

    @Karate.Test
    Karate booker() {
        return Karate.run().relativeTo(getClass());
    }
}
