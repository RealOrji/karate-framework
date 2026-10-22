@booker @booking @regression
Feature: Create booking - POST /booking

  Background:
    * url bookerUrl
    * path 'booking'
    * configure headers = { Accept: 'application/json' }
    * def bookingSchema = read('classpath:schemas/booker/booking.json')
    * def token = karate.callSingle('classpath:common/booker/get-token.feature').token
    * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')

  @TC-BKG-001 @smoke @positive @contract
  Scenario: Create a booking with a valid payload and verify it is persisted
    * def payload = buildBooking()

    Given request payload
    When method post
    Then status 200
    And match response == { bookingid: '#number', booking: '#(bookingSchema)' }
    And match response.booking == payload
    And assert responseTime < maxResponseTime
    * def bookingId = response.bookingid

    # Read it back: the API must have stored exactly what we sent.
    Given path 'booking', bookingId
    When method get
    Then status 200
    And match response == payload

  @TC-BKG-002 @positive @data-driven
  Scenario Outline: Create a booking - <description>
    * def stay = { checkin: '#(DataFactory.dateFromToday(30))', checkout: '#(DataFactory.dateFromToday(30 + nights))' }
    * def payload = buildBooking({ firstname: firstname, totalprice: totalprice, depositpaid: depositpaid, additionalneeds: additionalneeds, bookingdates: stay })

    Given request payload
    When method post
    Then status 200
    And match response.booking == payload
    And match response.booking == bookingSchema
    * def bookingId = response.bookingid

    Examples:
      | read('classpath:data/booker/booking-variations.json') |

  @TC-BKG-003 @negative
  Scenario Outline: Booking is rejected when mandatory field "<field>" is missing
    * def payload = buildBooking()
    * remove payload $.<field>

    Given request payload
    When method post
    Then status 500
    And match response == 'Internal Server Error'

    Examples:
      | field        |
      | firstname    |
      | lastname     |
      | totalprice   |
      | depositpaid  |
      | bookingdates |

  @TC-BKG-004 @positive
  Scenario: Optional field additionalneeds can be omitted
    * def payload = buildBooking()
    * remove payload $.additionalneeds

    Given request payload
    When method post
    Then status 200
    And match response.booking == payload
    And match response.booking.additionalneeds == '#notpresent'
    * def bookingId = response.bookingid

  @TC-BKG-005 @negative
  Scenario: Unsupported Accept header is rejected with 418
    # configure (not header) so it overrides the Accept header set in the Background
    * configure headers = { Accept: 'text/plain' }

    Given request buildBooking()
    When method post
    Then status 418
