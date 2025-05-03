(function() {
  'use strict';
  angular.module('appLvzViz').controller('SliderController', SliderController);

  SliderController.$inject = [ '$resource', '$interval' ];

  function SliderController($resource, $interval) {
    var vm = this;

    /* globals L */
    var map = L.map('mapStatistics').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    var heat = L.heatLayer([], {
      radius: 25,
      minOpacity: 0.5
    });

    vm.create = function() {
      $resource('api/minmaxdate').query({
        isArray: true
      }, function(minmaxdate) {
        $resource('api/last7days').query({
          isArray: true
        }, function(last7days) {
          map._onResize();
          createSlider(minmaxdate, last7days);
          var min = new Date(last7days[0].year, last7days[0].monthOfYear - 1, last7days[0].dayOfMonth);
          var max = new Date(last7days[1].year, last7days[1].monthOfYear - 1, last7days[1].dayOfMonth);
          getResources(min.toISOString(), max.toISOString());
        });
      });
    };

    function createSlider(minmaxdate, defaultDate) {
      $('#slider').dateRangeSlider({
        arrows: false,
        bounds: {
          min: new Date(minmaxdate[0].year, minmaxdate[0].monthOfYear - 1, minmaxdate[0].dayOfMonth),
          max: new Date(minmaxdate[1].year, minmaxdate[1].monthOfYear - 1, minmaxdate[1].dayOfMonth)
        },
        step: {
          days: 1
        },
        defaultValues: {
          min: new Date(defaultDate[0].year, defaultDate[0].monthOfYear - 1, defaultDate[0].dayOfMonth),
          max: new Date(defaultDate[1].year, defaultDate[1].monthOfYear - 1, defaultDate[1].dayOfMonth)
        }
      }).bind('valuesChanged', function(e, data) {
        getResources(data.values.min.toISOString(), data.values.max.toISOString());
        map._onResize();
      });
    }

    var promise;
    var delay = 150;

    var getUpperBound = function() {
      var bound = $('#slider').dateRangeSlider('bounds').max;
      console.debug('getUpperBound: ' + bound);
      return bound.getTime();
    };

    var getLowerBound = function() {
      var bound = $('#slider').dateRangeSlider('bounds').min;
      console.debug('getLowerBound: ' + bound);
      return bound.getTime();
    };

    var getUpperValue = function() {
      var value = $('#slider').dateRangeSlider('values').max;
      console.debug('getUpperValue: ' + value);
      return value.getTime();
    };

    var getLowerValue = function() {
      var value = $('#slider').dateRangeSlider('values').min;
      console.debug('getLowerValue: ' + value);
      return value.getTime();
    };

    vm.runforward = function() {
      if (getUpperValue() < getUpperBound()) {
        runforwardnow();
      }
    };

    vm.runforwardautomatic = function() {
      console.debug('runforwardautomatic');
      if (getUpperValue() < getUpperBound()) {
        promise = $interval(runforwardnow, delay);
      }
    };

    var runforwardnow = function() {
      console.debug('runforwardnow');
      var sl = $('#slider').dateRangeSlider('values');
      if (sl.max.getTime() < getUpperBound()) {
        var min = sl.min;
        min.setDate(min.getDate() + 1);
        var max = sl.max;
        max.setDate(max.getDate() + 1);
        $('#slider').dateRangeSlider('values', min, max);
      } else {
        vm.stop();
      }
    };

    vm.runbackward = function() {
      if (getLowerValue() > getLowerBound()) {
        runbackwardnow();
      }
    };

    vm.runbackwardautomatic = function() {
      console.debug('runbackwardautomatic');
      if (getLowerValue() > getLowerBound()) {
        promise = $interval(runbackwardnow, delay);
      }
    };

    var runbackwardnow = function() {
      console.debug('runbackwardnow');
      var sl = $('#slider').dateRangeSlider('values');
      if (sl.min.getTime() > getLowerBound()) {
        var min = sl.min;
        min.setDate(min.getDate() - 1);
        var max = sl.max;
        max.setDate(max.getDate() - 1);
        $('#slider').dateRangeSlider('values', min, max);
      } else {
        vm.stop();
      }
    };

    vm.stop = function() {
      console.debug('stop');
      $interval.cancel(promise);
    };

    var addtoMap = function(data) {
      var content = data.content;
      var latlng = [];
      for (var i = content.length - 1; i >= 0; i--) {
        var c = content[i];
        if (c.coords) {
          latlng.push([c.coords.lat, c.coords.lon]);
        }
      }
      heat.setLatLngs(latlng);
      if (map._size.y === 0) {} else {
        map.addLayer(heat);
      }
    };

    function getResources(from, to) {
      $resource('api/searchbetween').get({
        from: from,
        to: to,
        query: vm.query
      }, function(data) {
        console.debug(data.content.length + ' elements for query: "' + vm.query + '" (' + from + ' to ' + to + ')');
        addtoMap(data);
      });
    }
  }
})();
