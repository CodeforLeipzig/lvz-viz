var app = angular.module("lvzViz", ['ngResource', 'ui.bootstrap']);
app.controller('lvzVizCtrl', function($scope, $resource, search, getx) {
    $scope.totalItems = 64;
    $scope.currentPage = 4;
    var map = L.map('map').setView([51.339695, 12.373075], 10);
    // // add an OpenStreetMap tile layer
    var u_id = 'paesku.jilhmmgd';
    //  L.tileLayer('http://a.tiles.mapbox.com/v3/' + u_id + '/{z}/{x}/{y}.png', {
    //     attribution: ' & copy; <a href="https://www.mapbox.com/about/maps/" target="_blank">© Mapbox © OpenStreetMap</a> contributors'
    // }).addTo(map);
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
    var markers = new L.FeatureGroup();
    // var sidebar = L.control.sidebar('sidebar', {
    //     position: 'right'
    // });
    // map.addControl(sidebar);
    // sidebar.show();
    getx.get({
        page: 0,
        size: 30,
        sort: 'datePublished,desc'
    }, function(data) {
        data.number = data.number + 1;
        $scope.data = data
        addtoMap(data);
    })
    $scope.search = function(number) {
        $scope.searchquery = $scope.query
        search.get({
            'query': $scope.query,
            page: $scope.data.number - 1,
            size: 30,
            sort: 'datePublished,desc'
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
                var marker = new L.marker([c.coords.lat, c.coords.lon]).bindPopup("<a href=" + c.url + ">" + c.title + "</a><br>" + c.snippet);
                markers.addLayer(marker);
            }
        };
        map.addLayer(markers)
    }
    $scope.pageChanged = function() {
        if ($scope.query === undefined) {
            getx.get({
                page: $scope.data.number - 1,
                size: 30,
                sort: 'datePublished,desc'
            }, function(data) {
                data.number = data.number + 1;
                $scope.data = data
                addtoMap(data);
            })
        } else {
            $scope.search($scope.data.number - 1);
        }
    };
});
app.factory('search', function($resource) {
    return $resource('api/search', {
        'query': '@query',
        page: '@page',
        size: '@size',
        sort: '@sort'
    });
})
app.factory('getx', function($resource) {
    return $resource('api/getx', {
        page: '@page',
        size: '@size',
        sort: '@sort'
    });



})
