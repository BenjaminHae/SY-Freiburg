var map;
var geocoder;
var pinImage;
function initialize() {
  geocoder = new google.maps.Geocoder();
  var mapOptions = {
    zoom: 15,
    center: new google.maps.LatLng(47.99486,7.84992),
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  map = new google.maps.Map(document.getElementById('map-canvas'),
      mapOptions);
	pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + "0000FF",
        new google.maps.Size(21, 34),
        new google.maps.Point(0,0),
        new google.maps.Point(10, 34));
}
google.maps.event.addDomListener(window, 'load', initialize);
function codeAddress(address) {
  geocoder.geocode( { 'address': address}, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      map.setCenter(results[0].geometry.location);
	  document.getElementById('position').value = results[0].geometry.location;
	  document.getElementById('position').value= document.getElementById('position').value.substr(1,document.getElementById('position').value.length-2);
    } else {
      alert('Geocode was not successful for the following reason: ' + status);
    }
  });
}

function codeLatLng(input) {
  var latlngStr = input.split(',', 2);
  var lat = parseFloat(latlngStr[0]);
  var lng = parseFloat(latlngStr[1]);
  var latlng = new google.maps.LatLng(lat, lng);
  map.setCenter(latlng);
  geocoder.geocode({'latLng': latlng}, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      if (results[1]) {
		document.getElementById('address').value=results[1].formatted_address;
      } else {
        //alert('No results found');
      }
    } else {
      alert('Geocoder failed due to: ' + status);
    }
  });
}
function parseLatLong(str)
{
	var latlngStr = str.split(',', 2);
	var lat = parseFloat(latlngStr[0]);
	var lng = parseFloat(latlngStr[1]);
	return new google.maps.LatLng(lat, lng);
}
function addMarkerByPos(mname, pos)
{
	return addMarkerByPos(mname, pos, false);
}
function addMarkerByPos(mname, pos, colored)
{
	myLatLng=parseLatLong(pos);
	pIcon = null;
	if (colored)
	{
		pIcon=pinImage;
	}
	var newMarker = new google.maps.Marker({
      position: myLatLng,
      map: map,
      title: mname,
	  icon: pIcon
	 });
	return newMarker;
}
function removeMarker(marker)
{
	marker.setMap(null);
}
function drawWay(points, color)
{
  var flightPlanCoordinates = [
    new google.maps.LatLng(37.772323, -122.214897),
    new google.maps.LatLng(21.291982, -157.821856),
    new google.maps.LatLng(-18.142599, 178.431),
    new google.maps.LatLng(-27.46758, 153.027892)
  ];
  var color = '#FF0000';
  var way = new google.maps.Polyline({
    path: points,
    geodesic: true,
    strokeColor: color,
    strokeOpacity: 1.0,
    strokeWeight: 2
  });

  way.setMap(map);
  return way;
}
function addInfoBox(marker, str)
{           
        var boxText = document.createElement("div");
        //boxText.style.cssText = "border: 1px solid black; margin-top: 8px; background: white; padding: 5px;";
        boxText.innerHTML = str;
                
        var infowindow = new google.maps.InfoWindow({
      		content: boxText
  			});
			google.maps.event.addListener(marker, 'click', function() {
    		infowindow.open(map,marker);
  			});
        return infowindow;
}
function GetLocation(ondata, onfail)
{
 // Try HTML5 geolocation
  if(navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      var pos = new google.maps.LatLng(position.coords.latitude,
                                       position.coords.longitude);
		ondata(pos);
      map.setCenter(pos);
    }, function() {onfail(true)} );
  } else {
    // Browser doesn't support Geolocation
    onfail(false);
  }
}