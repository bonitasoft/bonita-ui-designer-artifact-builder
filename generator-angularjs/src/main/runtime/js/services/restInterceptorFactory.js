(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').factory('restInterceptorFactory', ['$q', '$location', '$window', '$filter', function ($q, $location, $window, $filter) {
    let getParentWindowIfBonitaApp = function (currentWindow, urlContext) {
      let parentWindowURL = currentWindow.parent.location.href;
      if (parentWindowURL.startsWith(urlContext)) {
        return currentWindow.parent;
      }
      return currentWindow;
    };
    return {
      'responseError': function (rejection) {
        let pageURL = $location.absUrl();
        let urlContext = pageURL.substring(0, pageURL.indexOf($location.path()));
        //if the REST request was for the same webapp as the page
        if (rejection.config.url.startsWith(urlContext) || rejection.config.url.startsWith('../API/')) {
          //same URL context as the page so this is a call to Bonita API
          let windowToRefresh = $window;
          if (windowToRefresh.parent !== windowToRefresh) {
            //we are in an Iframe
            //parent frame may be part of Bonita app (e.g. caselist)
            windowToRefresh = getParentWindowIfBonitaApp(windowToRefresh, urlContext);
            if (windowToRefresh.parent !== windowToRefresh) {
              //there is at least one more iframe
              //parent frame may be part of Bonita app (e.g. layout)
              windowToRefresh = getParentWindowIfBonitaApp(windowToRefresh, urlContext);
            }
          }
          switch (rejection.status) {
            case 401:
              if (confirm($filter('uiTranslate')('Your session is no longer active. You are going to be redirected.'))) {
                //reload the page in order for the authentication filter to redirect to the login page
                windowToRefresh.location.reload();
              }
              break;
            case 503:
              if (confirm($filter('uiTranslate')('Server is under maintenance. You are going to be redirected.'))) {
                //reload the page in order for the authentication filter to redirect to the maintenance page
                windowToRefresh.location.reload();
              }
              break;
          }
          return $q.reject(rejection);
        }
      }
    };
  }]);
})();