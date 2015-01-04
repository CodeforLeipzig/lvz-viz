app.controller('sliderctrl', function($scope, $filter, $resource, $interval) {
    var dates = {}
    var last7days = {}
    var map2 = L.map('map2').setView([51.339695, 12.373075], 11);
    // // add an OpenStreetMap tile layer
    var u_id = 'paesku.jilhmmgd';
    L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map2);
    var markers = new L.FeatureGroup();
    var heat = L.heatLayer([], {
        radius: 25,
        minOpacity: 0.5
    });
    $scope.create = function() {
        $resource('api/minmaxdate').query({
            isArray: true
        }, function(data) {
            // console.log(data)
            dates = data
            $resource('api/last7days').query({
                isArray: true
            }, function(data) {
                map2._onResize()
                last7days = data
                createSlider(dates, last7days);
                var min = new Date(last7days[0].year, last7days[0].monthOfYear - 1, last7days[0].dayOfMonth)
                var max = new Date(last7days[1].year, last7days[1].monthOfYear - 1, last7days[1].dayOfMonth)
                getResources(min.toISOString(), max.toISOString());
            })
        })
    }

    function createSlider(minmaxdate, defaultDate) {
        $("#slider ").dateRangeSlider({
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
        }).bind("valuesChanged", function(e, data) {
            getResources(data.values.min.toISOString(), data.values.max.toISOString())
            map2._onResize()
        });
    }
    var promise;
    var delay = 150;
    /**
     * Forward running
     */
    $scope.runforward = function() {
        var upperbound = new Date($("#slider ").dateRangeSlider("bounds").max).getTime();
        var maxcheck = new Date($("#slider ").dateRangeSlider("values").max).getTime();
        if (maxcheck < upperbound) {
            runforwardnow()
        }
    }
    $scope.runforwardautomatic = function() {
        var upperbound = new Date($("#slider ").dateRangeSlider("bounds").max).getTime();
        var maxcheck = new Date($("#slider ").dateRangeSlider("values").max).getTime();
        if (maxcheck < upperbound) {
            promise = $interval(runforwardnow, delay);
        } else {
            $interval.cancel(promise);
        }
    }
    var runforwardnow = function() {
        var upperbound = new Date($("#slider ").dateRangeSlider("bounds").max).getTime();
        var maxcheck = new Date($("#slider ").dateRangeSlider("values").max).getTime();
        if (maxcheck < upperbound) {
            sl = $("#slider ").dateRangeSlider("values");
            maxcheck = new Date(sl.max).getTime();
            min = new Date(sl.min);
            min.setDate(min.getDate() + 1);
            max = new Date(sl.max);
            max.setDate(max.getDate() + 1);
            $("#slider ").dateRangeSlider("values", min, max)
        }
    }
    /**
     * Backward running
     */
    $scope.runbackward = function() {
        var lowerbound = new Date($("#slider ").dateRangeSlider("bounds").min).getTime();
        var mincheck = new Date($("#slider ").dateRangeSlider("values").min).getTime();
        if (mincheck > lowerbound) {
            runbackwardnow()
        }
    }
    $scope.runbackwardautomatic = function() {
        var lowerbound = new Date($("#slider ").dateRangeSlider("bounds").min).getTime();
        var mincheck = new Date($("#slider ").dateRangeSlider("values").min).getTime();
        if (mincheck > lowerbound) {
            promise = $interval(runbackwardnow, delay);
        } else {
            $interval.cancel(promise);
        }
    }
    var runbackwardnow = function() {
        var sl = $("#slider ").dateRangeSlider("values");
        var lowerbound = new Date($("#slider ").dateRangeSlider("bounds").min).getTime();
        var mincheck = new Date(sl.min).getTime();
        if (mincheck > lowerbound) {
            mincheck = new Date(sl.min).getTime();
            var min = new Date(sl.min);
            min.setDate(min.getDate() - 1);
            var max = new Date(sl.max);
            max.setDate(max.getDate() - 1);
            $("#slider ").dateRangeSlider("values", min, max)
        }
    }
    $scope.stop = function() {
        console.log("stop")
        $interval.cancel(promise)
    }
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
        content = data.content;
        var latlng = [];
        for (var i = content.length - 1; i >= 0; i--) {
            var c = content[i];
            if (c.coords === null) {} else {
                latlng.push([c.coords.lat, c.coords.lon])
            }
        };
        heat.setLatLngs(latlng)
        if (map2._size.y === 0) {} else {
            map2.addLayer(heat)
        }
    }
    $scope.search = function() {
        var sl = $("#slider ").dateRangeSlider("values");
        getResources(sl.min.toISOString(), sl.max.toISOString());
    }

    function getResources(from, to) {
        $resource('api/searchbetween').get({
            from: from.toString(),
            to: to.toString(),
            query: $scope.query
        }, function(data) {
            console.debug(data.content.length + " Elements")
            addtoMap(data)
        });
    }
});