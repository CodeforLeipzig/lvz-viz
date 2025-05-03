(function() {
  'use strict';
  angular.module('appLvzViz').controller('SearchController', SearchController);

  SearchController.$inject = [ '$resource', 'search', 'getx' ];

  function SearchController($resource, search, getx) {
    var vm = this;

    /* globals L */
    var map = L.map('mapSearch').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    var markers = new L.FeatureGroup();
    getx.get({
      page: 0,
      size: 30,
      sort: 'datePublished,desc'
    }, function(data) {
      data.number = data.number + 1;
      vm.data = data;
      addtoMap(data);
    });
    $('#blubble').click(function(){
      console.log('blubble click');
      map._onResize();

    });

    vm.search = function() {
      console.log('search ' + vm.query + ' (' + vm.data.number + ')');
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
      map.removeLayer(markers);
      markers = new L.FeatureGroup();
      var content = data.content;
      for (var i = content.length - 1; i >= 0; i--) {
        var c = content[i];
        if (c.coords) {
          var marker = new L.marker([c.coords.lat, c.coords.lon]).bindPopup('<a href=' + c.url + '>' + c.title + '</a><br>' + c.snippet);
          markers.addLayer(marker);
        }
      }
      map.addLayer(markers);
    };

    vm.pageChanged = function() {
      console.log('pageChanged');
      if (vm.query === undefined) {
        getx.get({
          page: vm.data.number - 1,
          size: 30,
          sort: 'datePublished,desc'
        }, function(data) {
          data.number = data.number + 1;
          vm.data = data;
          addtoMap(data);
        });
      } else {
        // vm.data.number - 1
        vm.search();
      }
    };
  }

  angular.module('appLvzViz').factory('search', search);

  search.$inject = [ '$resource' ];

  function search($resource) {
    return $resource('api/search', {
      'query': '@query',
      page: '@page',
      size: '@size',
      sort: '@sort'
    });
  }


  angular.module('appLvzViz').factory('getx', getx);

  getx.$inject = [ '$resource' ];

  function getx($resource) {
    return $resource('api/getx', {
      page: '@page',
      size: '@size',
      sort: '@sort'
    });
  }
})();
