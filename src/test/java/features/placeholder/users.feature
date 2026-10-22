@placeholder @users @regression
Feature: Users - JSONPlaceholder /users

  Background:
    * url placeholderUrl
    * def userSchema = read('classpath:schemas/placeholder/user.json')

  @TC-USR-001 @smoke @contract
  Scenario: List users returns all 10 users matching the user schema
    Given path 'users'
    When method get
    Then status 200
    And match response == '#[10]'
    And match each response == userSchema
    And match response[*].id == karate.range(1, 10)
    And assert responseTime < maxResponseTime

  @TC-USR-002 @positive @data-driven
  Scenario Outline: Get user <userId> by id
    Given path 'users', userId
    When method get
    Then status 200
    And match response == userSchema
    And match response contains { id: '#(~~userId)', username: '<expectedUsername>' }

    Examples:
      | read('classpath:data/placeholder/user-ids.csv') |

  @TC-USR-003 @negative
  Scenario: Unknown user id returns 404 with an empty body
    Given path 'users', 9999
    When method get
    Then status 404
    And match response == {}

  @TC-USR-004 @positive
  Scenario: Filter users by username
    Given path 'users'
    And param username = 'Bret'
    When method get
    Then status 200
    And match response == '#[1]'
    And match response[0] contains { id: 1, username: 'Bret', email: 'Sincere@april.biz' }
