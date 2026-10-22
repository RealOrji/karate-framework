@booker @booking @regression
Feature: Read bookings - GET /booking and GET /booking/{id}

  Background:
    * url bookerUrl
    * configure headers = { Accept: 'application/json' }
    * def bookingSchema = read('classpath:schemas/booker/booking.json')
    * def token = karate.callSingle('classpath:common/booker/get-token.feature').token
    * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')

  @TC-BKG-006 @smoke @contract
  Scenario: List all bookings returns an array of booking ids
    Given path 'booking'
    When method get
    Then status 200
    And match response == '#[_ > 0]'
    And match each response == { bookingid: '#number' }
    And assert responseTime < maxResponseTime

  @TC-BKG-007 @positive @contract
  Scenario: Get a booking by id returns the full booking
    * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking())' }
    * def bookingId = created.bookingId

    Given path 'booking', bookingId
    When method get
    Then status 200
    And match response == bookingSchema
    And match response == created.booking

  @TC-BKG-008 @positive
  Scenario: Filter bookings by guest first and last name
    * def guest = buildBooking()
    * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(guest)' }
    * def bookingId = created.bookingId

    Given path 'booking'
    And params { firstname: '#(guest.firstname)', lastname: '#(guest.lastname)' }
    When method get
    Then status 200
    # lastname is unique per run, so our booking must be the only match
    And match response == [{ bookingid: '#(bookingId)' }]

  @TC-BKG-009 @negative
  Scenario Outline: Unknown booking id "<id>" returns 404
    Given path 'booking', '<id>'
    When method get
    Then status 404
    And match response == 'Not Found'

    Examples:
      | id        |
      | 999999999 |
      | abc       |
      | 0         |
