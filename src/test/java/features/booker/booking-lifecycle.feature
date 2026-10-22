@booker @e2e @regression
Feature: Booking lifecycle - end to end

  One guest journey across every endpoint, chaining data from each step to the next.

  Background:
    * url bookerUrl
    * configure headers = { Accept: 'application/json' }
    * def bookingSchema = read('classpath:schemas/booker/booking.json')
    * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')

  @TC-E2E-001 @smoke
  Scenario: Guest books, searches, amends and cancels a stay
    # 1. Authenticate
    * def auth = call read('classpath:common/booker/get-token.feature')
    * def token = auth.token

    # 2. Create the booking
    * def guest = buildBooking({ depositpaid: false })
    Given path 'booking'
    And request guest
    When method post
    Then status 200
    And match response.booking == guest
    * def bookingId = response.bookingid

    # 3. Find it by guest name
    Given path 'booking'
    And params { firstname: '#(guest.firstname)', lastname: '#(guest.lastname)' }
    When method get
    Then status 200
    And match response contains { bookingid: '#(bookingId)' }

    # 4. Guest pays the deposit and extends the stay by two nights
    * def extendedStay = { checkin: '#(guest.bookingdates.checkin)', checkout: '#(DataFactory.dateFromToday(19))' }
    * def amended = karate.merge(guest, { depositpaid: true, bookingdates: extendedStay })
    Given path 'booking', bookingId
    And cookie token = token
    And request amended
    When method put
    Then status 200
    And match response == amended

    # 5. Guest changes a special request only
    Given path 'booking', bookingId
    And cookie token = token
    And request { additionalneeds: 'Vegan breakfast' }
    When method patch
    Then status 200
    And match response == karate.merge(amended, { additionalneeds: 'Vegan breakfast' })
    And match response == bookingSchema

    # 6. Guest cancels
    Given path 'booking', bookingId
    And cookie token = token
    When method delete
    Then status 201

    Given path 'booking', bookingId
    When method get
    Then status 404
