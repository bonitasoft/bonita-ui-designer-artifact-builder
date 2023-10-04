(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .controller('httpErrorModalCtrl', function ($scope, $modalInstance, messages) {

      $scope.messages = messages;

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

      $scope.confirm = function () {
        $modalInstance.close();
      };
    });

})();