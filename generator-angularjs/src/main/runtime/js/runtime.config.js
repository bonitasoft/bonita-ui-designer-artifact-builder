(function() {
  'use strict';

  angular.module('bonitasoft.ui.services').config($httpProvider => {
    // configure bonita XSRF token cookie and header names
    $httpProvider.defaults.xsrfHeaderName = $httpProvider.defaults.xsrfCookieName = 'X-Bonita-API-Token';
    // configure interceptor to handle errors when calling Bonita REST APIs
    $httpProvider.interceptors.push('restInterceptorFactory');
  });
})();

