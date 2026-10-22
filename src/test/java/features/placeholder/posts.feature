@placeholder @posts @regression
Feature: Posts - JSONPlaceholder /posts

  JSONPlaceholder fakes writes: they respond like a real API but nothing is persisted,
  so write scenarios assert on the response only.

  Background:
    * url placeholderUrl
    * def postSchema = read('classpath:schemas/placeholder/post.json')
    * def commentSchema = read('classpath:schemas/placeholder/comment.json')

  @TC-PST-001 @positive @data-driven
  Scenario Outline: Every post returned for user <userId> belongs to that user
    Given path 'posts'
    And param userId = userId
    When method get
    Then status 200
    And match response == '#[10]'
    And match each response == postSchema
    And match each response contains { userId: '#(~~userId)' }

    Examples:
      | userId |
      | 1      |
      | 4      |
      | 10     |

  @TC-PST-002 @smoke @positive
  Scenario: Create a post
    * def post = buildPost()

    Given path 'posts'
    And request post
    When method post
    Then status 201
    And match response == karate.merge(post, { id: '#number' })
    And match responseHeaders['Content-Type'][0] contains 'application/json'

  @TC-PST-003 @positive
  Scenario: Replace a post with PUT
    * def post = buildPost({ id: 1, userId: 1 })

    Given path 'posts', 1
    And request post
    When method put
    Then status 200
    And match response == post

  @TC-PST-004 @positive
  Scenario: Patch only the title of a post
    Given path 'posts', 1
    When method get
    Then status 200
    * def original = response

    Given path 'posts', 1
    And request { title: 'Patched by Karate' }
    When method patch
    Then status 200
    And match response == karate.merge(original, { title: 'Patched by Karate' })

  @TC-PST-005 @positive
  Scenario: Delete a post
    Given path 'posts', 1
    When method delete
    Then status 200
    And match response == {}

  @TC-PST-006 @contract
  Scenario: Comments of a post match the comment schema and reference the post
    Given path 'posts', 1, 'comments'
    When method get
    Then status 200
    And match response == '#[_ > 0]'
    And match each response == commentSchema
    And match each response contains { postId: 1 }
