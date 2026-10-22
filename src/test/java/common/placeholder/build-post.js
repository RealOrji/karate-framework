/**
 * Builds a JSONPlaceholder post payload. Any field can be overridden:
 *   * def post = buildPost({ userId: 3 })
 */
function(overrides) {
  var post = {
    userId: 1,
    title: 'Karate post ' + DataFactory.randomLetters(6),
    body: 'Created by the Karate API framework at ' + new Date().toISOString()
  };
  return karate.merge(post, overrides || {});
}
