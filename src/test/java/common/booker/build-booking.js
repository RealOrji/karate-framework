/**
 * Builds a valid, unique Restful-Booker payload. Any field can be overridden:
 *   * def payload = buildBooking()
 *   * def payload = buildBooking({ depositpaid: false, totalprice: 0 })
 */
function(overrides) {
  var booking = {
    firstname: DataFactory.firstName(),
    lastname: DataFactory.uniqueName('Karate'),
    totalprice: DataFactory.price(100, 900),
    depositpaid: true,
    bookingdates: {
      checkin: DataFactory.dateFromToday(14),
      checkout: DataFactory.dateFromToday(17)
    },
    additionalneeds: DataFactory.additionalNeeds()
  };
  return karate.merge(booking, overrides || {});
}
