(function() {
  'use strict';
  angular.module('appLvzViz').directive('myButtons', function() {
    return {
      templateUrl: 'templates/dateRangeButtons.html'
    };
  }).directive('myResultlist', function() {
    return {
      templateUrl: 'templates/myResultlist.html'
    };
  });
})();
