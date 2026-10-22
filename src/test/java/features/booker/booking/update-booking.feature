@booker @booking @regression
Feature: Update booking - PUT and PATCH /booking/{id}

  Updates require either a token cookie or Basic auth. Every scenario starts from a
  fresh booking created in the Background and removed again after the scenario.

  Background:
    * url bookerUrl
    * configure headers = { Accept: 'application/json' }
    * def bookingSchema = read('classpath:schemas/booker/booking.json')
    * def token = karate.callSingle('classpath:common/booker/get-token.feature').token
    * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking())' }
    * def bookingId = created.bookingId
    * def original = created.booking
    * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')

  @TC-BKG-010 @smoke @positive
  Scenario: Full update with a token replaces every field
    * def changes = buildBooking({ depositpaid: !original.depositpaid })

    Given path 'booking', bookingId
    And cookie token = token
    And request changes
    When method put
    Then status 200
    And match response == changes
    And match response == bookingSchema

    Given path 'booking', bookingId
    When method get
    Then status 200
    And match response == changes

  @TC-BKG-011 @positive
  Scenario: Partial update changes only the patched fields
    * def patch = { firstname: 'Patched', totalprice: 999 }
    * def expected = karate.merge(original, patch)

    Given path 'booking', bookingId
    And cookie token = token
    And request patch
    When method patch
    Then status 200
    And match response == expected

  @TC-BKG-012 @positive
  Scenario: Full update with Basic auth instead of a token
    * def changes = buildBooking()

    Given path 'booking', bookingId
    And header Authorization = AuthHeaders.basic(credentials.username, credentials.password)
    And request changes
    When method put
    Then status 200
    And match response == changes

  @TC-BKG-013 @negative @security
  Scenario: Update without credentials is forbidden
    Given path 'booking', bookingId
    And request buildBooking()
    When method put
    Then status 403
    And match response == 'Forbidden'

    # The booking must be untouched
    Given path 'booking', bookingId
    When method get
    Then match response == original

  @TC-BKG-014 @negative @security
  Scenario Outline: Update with an invalid token "<badToken>" is forbidden
    Given path 'booking', bookingId
    And cookie token = '<badToken>'
    And request { firstname: 'Hacker' }
    When method patch
    Then status 403

    Examples:
      | badToken         |
      | invalid          |
      | 000000000000000  |
      | expired-token-42 |

  @TC-BKG-015 @negative
  Scenario: Full update with an incomplete body is rejected
    Given path 'booking', bookingId
    And cookie token = token
    And request { firstname: 'OnlyFirstName' }
    When method put
    Then status 400
    And match response == 'Bad Request'
