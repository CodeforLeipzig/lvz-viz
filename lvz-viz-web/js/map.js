var app = angular.module("lvzViz", ['ngResource', 'ui.bootstrap']);
app.controller('lvzVizCtrl', function($scope, $resource, search, getx) {
    $scope.totalItems = 64;
    $scope.currentPage = 4;
    var map = L.map('map').setView([51.339695, 12.373075], 11);
    // // add an OpenStreetMap tile layer
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
    var markers = new L.FeatureGroup();
    getx.get({
        page: 0,
        x: 6
    }, function(data) {
        console.log(data)
        data.number = data.number + 1;
        $scope.data = data
        addtoMap(data);
    })
    $scope.search = function() {
        console.log($scope.query)
        search.get({
            'query': $scope.query,
            'page': 0,
            'limit': 6
        }, function(data) {
            data.number = data.number + 1;
            $scope.data = data
            addtoMap(data);
        })
    }
    var addtoMap = function(data) {
        map.removeLayer(markers);
        markers = new L.FeatureGroup();
        content = data.content;
        for (var i = content.length - 1; i >= 0; i--) {
            var c = content[i];
            if (c.coords === null) {} else {
                var marker = new L.marker([c.coords.lat, c.coords.lon]).bindPopup("<a href=" + c.url + ">" + c.title + "</a>");
                markers.addLayer(marker);
            }
        };
        map.addLayer(markers)
    }
    $scope.pageChanged = function() {
        console.log('Page changed to: ' + $scope.data.number);
        getx.get({
            page: $scope.data.number - 1,
            x: 6
        }, function(data) {
            console.log(data)
            data.number = data.number + 1;
            $scope.data = data
            addtoMap(data);
        })
    };
});
app.factory('search', function($resource) {
    return $resource('api/search', {
        'query': '@query',
        'page': '@page',
        'limit': '@limit'
    });
})
app.factory('getx', function($resource) {
    return $resource('api/getx', {
        page: '@page',
        x: '@x'
    });
})