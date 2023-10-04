(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').factory('restInterceptorFactory', ['$q', '$location', '$window', '$filter', '$injector', function ($q, $location, $window, $filter, $injector) {
    var $modal;
    var confirmationModalIsOpen = false;

    var openConfirmationModal = function (redirectMessage, cancelMessage, windowToRefresh) {
      try {
        $modal = $modal || $injector.get('$modal');
        confirmationModalIsOpen = true;
        var confirmModal = $modal.open({
          controller: 'httpErrorModalCtrl',
          size: 'md',
          resolve: {
            messages: function () {
              return {
                'redirect': redirectMessage,
                'cancel': cancelMessage
              };
            }
          },
          // Template needs to be defined here instead of external file because the request to templateURL would also fail with 401 or 503
          template: '<div class="modal-header">\n' +
            '    <h3 class="modal-title">{{\'An error occurred with the requested operation\' | uiTranslate}}</h3>\n' +
            '</div>\n' +
            '<div class="modal-body">\n' +
            '    <p>{{messages.redirect}}</p>\n' +
            '    <p>{{messages.cancel}}</p>\n' +
            '</div>\n' +
            '<div class="modal-footer">\n' +
            '    <div>\n' +
            '        <button id="confirm" type="submit" class="btn btn-primary" ng-click="confirm()" >{{\'OK\' | uiTranslate}}</button>\n' +
            '        <button id="cancel" type="submit" class="btn btn-default" ng-click="cancel()">{{\'Cancel\' | uiTranslate}}</button>\n' +
            '    </div>\n' +
            '</div>\n' +
            '</div>'
        });
        confirmModal.result.then(
          function () {
            //reload the page in order for the authentication filter to redirect to the login or maintenance page
            windowToRefresh.location.reload();
          }, function () {
            confirmationModalIsOpen = false;
          });
      } catch (e) {
        // In case there is an issue with the modal
        windowToRefresh.location.reload();
      }
    };

    let getParentWindowIfBonitaApp = function (currentWindow, urlContext) {
      let parentWindowURL = currentWindow.parent.location.href;
      if (parentWindowURL.startsWith(urlContext)) {
        return currentWindow.parent;
      }
      return currentWindow;
    };

    return {
      'responseError': function (rejection) {
        if (!confirmationModalIsOpen) {
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
                openConfirmationModal($filter('uiTranslate')('Your session is no longer active. Click on OK to be redirected and log back in.'),
                  $filter('uiTranslate')('Click on Cancel to remain on this page and try to execute the operation again once you logged back in (e.g. in another tab).'),
                  windowToRefresh);
                break;
              case 503:
                openConfirmationModal($filter('uiTranslate')('Server is under maintenance. Click on OK to be redirected to the maintenance page.'),
                  $filter('uiTranslate')('Click on Cancel to remain on this page and wait for the maintenance to end.'), windowToRefresh);
                break;
            }
          }
        }
        return $q.reject(rejection);
      }
    };
  }]);
})();