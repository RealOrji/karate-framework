@booker @booking @regression
Feature: Delete booking - DELETE /booking/{id}

  Note: Restful-Booker answers a successful delete with 201 Created.

  Background:
    * url bookerUrl
    * configure headers = { Accept: 'application/json' }
    * def token = karate.callSingle('classpath:common/booker/get-token.feature').token
    * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')

  @TC-BKG-016 @smoke @positive
  Scenario: Delete a booking and confirm it no longer exists
    * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking())' }
    * def bookingId = created.bookingId

    Given path 'booking', bookingId
    And cookie token = token
    When method delete
    Then status 201

    Given path 'booking', bookingId
    When method get
    Then status 404

  @TC-BKG-017 @negative @security
  Scenario: Delete without credentials is forbidden and keeps the booking
    * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking())' }
    * def bookingId = created.bookingId

    Given path 'booking', bookingId
    When method delete
    Then status 403

    Given path 'booking', bookingId
    When method get
    Then status 200
    And match response == created.booking

  @TC-BKG-018 @negative
  Scenario: Deleting a booking that does not exist returns 405
    Given path 'booking', 999999999
    And cookie token = token
    When method delete
    Then status 405
