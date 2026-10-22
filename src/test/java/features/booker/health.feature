@booker @health @regression
Feature: Restful-Booker health check

  Confirms the service is up before anything else is trusted.
  Heroku free dynos sleep, so the first call retries while the app wakes up.

  Background:
    * url bookerUrl

  @TC-HLT-001 @smoke
  Scenario: Health check endpoint reports the service is up
    Given path 'ping'
    And retry until responseStatus == 201
    When method get
    Then status 201
    And match response == 'Created'
