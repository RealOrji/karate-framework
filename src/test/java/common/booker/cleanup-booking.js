/**
 * afterScenario hook: deletes the booking a scenario created, even when the scenario failed.
 *   * configure afterScenario = read('classpath:common/booker/cleanup-booking.js')
 * Expects the scenario to define `bookingId` and `token`.
 */
function() {
  var bookingId = karate.get('bookingId');
  if (!bookingId) return;
  karate.call('classpath:common/booker/delete-booking.feature', { bookingId: bookingId, token: karate.get('token') });
}
