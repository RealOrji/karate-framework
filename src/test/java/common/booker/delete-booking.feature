@ignore
Feature: Reusable - delete a booking (test data cleanup)

  # Input: bookingId, token
  # Idempotent: 405 means the booking was already gone, which is fine for cleanup.

  Scenario: Delete booking
    Given url bookerUrl
    And path 'booking', bookingId
    And cookie token = token
    When method delete
    Then assert responseStatus == 201 || responseStatus == 405
