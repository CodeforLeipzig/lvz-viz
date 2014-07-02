// create a map in the "map" div, set the view to a given place and zoom
var map = L.map('map').setView([51.339695, 12.373075], 11);

// add an OpenStreetMap tile layer
L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

// add a marker in the given location, attach some popup content to it and open the popup
L.marker([51.339695, 12.373075]).addTo(map)
    .bindPopup('A pretty CSS3 popup. <br> Easily customizable.')
    .openPopup();

    var popup = L.popup();

function onMapClick(e) {
    popup
        .setLatLng(e.latlng)
        .setContent("You clicked the map at " + e.latlng.toString())
        .openOn(map);
}

map.on('click', onMapClick);