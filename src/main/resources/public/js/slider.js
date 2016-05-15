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

    /**
     * Forward running
     */
    vm.runforward = function() {
      var upperbound = new Date($('#slider').dateRangeSlider('bounds').max).getTime();
      var maxcheck = new Date($('#slider').dateRangeSlider('values').max).getTime();
      if (maxcheck < upperbound) {
        runforwardnow();
      }
    };

    vm.runforwardautomatic = function() {
      var upperbound = new Date($('#slider').dateRangeSlider('bounds').max).getTime();
      var maxcheck = new Date($('#slider').dateRangeSlider('values').max).getTime();
      console.log('upperbound: ' + upperbound + ', maxcheck: ' + maxcheck);
      if (maxcheck < upperbound) {
        promise = $interval(runforwardnow, delay);
      } else {
        vm.stop();
      }
    };

    var runforwardnow = function() {
      var sl = $('#slider').dateRangeSlider('values');
      var upperbound = new Date($('#slider').dateRangeSlider('bounds').max).getTime();
      var maxcheck = new Date(sl.max).getTime();
      if (maxcheck < upperbound) {
        maxcheck = new Date(sl.max).getTime();
        var min = new Date(sl.min);
        min.setDate(min.getDate() + 1);
        var max = new Date(sl.max);
        max.setDate(max.getDate() + 1);
        $('#slider').dateRangeSlider('values', min, max);
      } else {
        vm.stop();
      }
    };

    /**
     * Backward running
     */
    vm.runbackward = function() {
      var lowerbound = new Date($('#slider').dateRangeSlider('bounds').min).getTime();
      var mincheck = new Date($('#slider').dateRangeSlider('values').min).getTime();
      if (mincheck > lowerbound) {
        runbackwardnow();
      }
    };

    vm.runbackwardautomatic = function() {
      var lowerbound = new Date($('#slider').dateRangeSlider('bounds').min).getTime();
      var mincheck = new Date($('#slider').dateRangeSlider('values').min).getTime();
      if (mincheck > lowerbound) {
        promise = $interval(runbackwardnow, delay);
      } else {
        vm.stop();
      }
    };

    var runbackwardnow = function() {
      var sl = $('#slider').dateRangeSlider('values');
      var lowerbound = new Date($('#slider').dateRangeSlider('bounds').min).getTime();
      var mincheck = new Date(sl.min).getTime();
      if (mincheck > lowerbound) {
        mincheck = new Date(sl.min).getTime();
        var min = new Date(sl.min);
        min.setDate(min.getDate() - 1);
        var max = new Date(sl.max);
        max.setDate(max.getDate() - 1);
        $('#slider').dateRangeSlider('values', min, max);
      } else {
        vm.stop();
      }
    };

    vm.stop = function() {
        console.log('stop');
        $interval.cancel(promise);
    };

    vm.search = function() {
      console.log('search1');
      vm.searchquery = vm.query;
      search.get({
        'query': vm.query,
        page: vm.data.number - 1,
        size: 30,
        sort: 'datePublished,desc'
      }, function(data) {
        data.number = data.number + 1;
        vm.data = data;
        addtoMap(data);
      });
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

    vm.search = function() {
      console.log('search2');
      var sl = $('#slider').dateRangeSlider('values');
      getResources(sl.min.toISOString(), sl.max.toISOString());
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
