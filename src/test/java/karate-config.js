function fn() {
  // Resolution order for any setting: -Dsystem.property > ENV_VARIABLE > default.
  var System = Java.type('java.lang.System');
  function setting(property, envVar, fallback) {
    return karate.properties[property] || System.getenv(envVar) || fallback;
  }

  var env = karate.env || 'qa';

  // One block per target environment. Every environment exposes the same keys,
  // so feature files never need to know which one they are running against.
  var environments = {
    dev: {
      bookerUrl: 'https://restful-booker.herokuapp.com',
      placeholderUrl: 'https://jsonplaceholder.typicode.com'
    },
    qa: {
      bookerUrl: 'https://restful-booker.herokuapp.com',
      placeholderUrl: 'https://jsonplaceholder.typicode.com'
    },
    staging: {
      bookerUrl: 'https://restful-booker.herokuapp.com',
      placeholderUrl: 'https://jsonplaceholder.typicode.com'
    }
  };

  if (!environments[env]) {
    karate.fail('Unknown karate.env "' + env + '". Expected one of: ' + Object.keys(environments));
  }

  var config = environments[env];
  config.env = env;
  config.bookerUrl = setting('booker.url', 'BOOKER_URL', config.bookerUrl);
  config.placeholderUrl = setting('placeholder.url', 'PLACEHOLDER_URL', config.placeholderUrl);

  // Restful-Booker publishes these demo credentials; real projects must supply them via CI secrets.
  config.credentials = {
    username: setting('booker.username', 'BOOKER_USERNAME', 'admin'),
    password: setting('booker.password', 'BOOKER_PASSWORD', 'password123')
  };

  // Performance budget used by response-time assertions (milliseconds).
  config.maxResponseTime = parseInt(setting('max.response.time', 'MAX_RESPONSE_TIME', '5000'));

  // Reusable helpers, available to every feature without an explicit read().
  config.DataFactory = Java.type('com.realorji.api.support.DataFactory');
  config.AuthHeaders = Java.type('com.realorji.api.support.AuthHeaders');
  config.buildBooking = read('classpath:common/booker/build-booking.js');
  config.buildPost = read('classpath:common/placeholder/build-post.js');

  karate.configure('connectTimeout', 15000);
  karate.configure('readTimeout', 30000);
  karate.configure('retry', { count: 5, interval: 3000 });
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);
  karate.configure('logModifier', Java.type('com.realorji.api.support.SensitiveDataMasker').INSTANCE);

  karate.log('karate.env =', env, '| booker =', config.bookerUrl, '| placeholder =', config.placeholderUrl);
  return config;
}
