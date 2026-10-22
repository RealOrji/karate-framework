@ignore
Feature: Reusable - create a booking

  # Input : payload  (build one with buildBooking())
  # Output: bookingId, booking (what the API stored), response
  #
  #   * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking())' }
  #   * def bookingId = created.bookingId

  Scenario: Create booking
    Given url bookerUrl
    And path 'booking'
    And header Accept = 'application/json'
    And request payload
    When method post
    Then status 200
    And match response == { bookingid: '#number', booking: '#(payload)' }
    * def bookingId = response.bookingid
    * def booking = response.booking
