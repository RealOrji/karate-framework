@ignore
Feature: Reusable - obtain a Restful-Booker auth token

  # Input : none - bookerUrl and credentials come from karate-config.js.
  #         Never pass credentials as a call argument: call arguments are written to the reports.
  # Output: token
  #
  # Use callSingle so the whole parallel run shares one token:
  #   * def token = karate.callSingle('classpath:common/booker/get-token.feature').token

  Scenario: Create auth token
    Given url bookerUrl
    And path 'auth'
    And request credentials
    When method post
    Then status 200
    And match response == { token: '#string' }
    * def token = response.token
