@booker @auth @regression
Feature: Restful-Booker authentication

  POST /auth exchanges credentials for a token used by update and delete operations.
  Note: the API answers bad credentials with HTTP 200 and a "reason" body, not 401.

  Background:
    * url bookerUrl
    * path 'auth'

  @TC-AUTH-001 @smoke @positive
  Scenario: Valid credentials return an auth token
    Given request credentials
    When method post
    Then status 200
    And match response == { token: '#regex [a-z0-9]{15}' }
    And assert responseTime < maxResponseTime

  @TC-AUTH-002 @negative @data-driven
  Scenario Outline: Login is rejected for <description>
    Given request { username: '<username>', password: '<password>' }
    When method post
    Then status 200
    And match response == { reason: 'Bad credentials' }

    Examples:
      | read('classpath:data/booker/invalid-credentials.csv') |

  @TC-AUTH-003 @negative
  Scenario: Login with an empty body is rejected
    Given request {}
    When method post
    Then status 200
    And match response == { reason: 'Bad credentials' }
