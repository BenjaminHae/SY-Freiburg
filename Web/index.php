<!DOCTYPE html>
<html>
	<head>
		<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
		<script type="text/javascript" src="map.js"></script>
		<script type="text/javascript" src="ajax.js"></script>
		<meta charset="utf-8">
		<style>
			html, body{ height:100%; margin:0px; padding:0px;}
			#map-canvas {height: 50%; margin:0px; padding:0px;}
			#insLoc { display:none; }
			#inputform, #right{float:left; margin-right:3em;}
			.comment {padding-left:1em;}
			.gpinput{width:4em;}
		</style>
	</head>
<body>
<div id="map-canvas"></div>
<input type="button" value="MisterX" onclick="ShowMisterX();"/>
<input type="button" value="Verfolger" onclick="ShowHGroups();"/>
<input type="button" value="Leeren" onclick="HideGroups();"/>
<br/>
<br/>
<div id="bottom">
<div id="inputform">
<input type="button" value="Neue Koordinaten eingeben" onclick="ShowInput();"/>
<form id="insLoc">
<b>Neue Position angeben</b><br/>
Gruppe <input type="text" name="group" id="group" class="gpinput"/><br/>
Adresse <input type="text" name="address" id="address"/><input type="button" name="bShowAdr" value="Anzeigen und Umrechnen" onclick="ShowAdr();"/><br/>
oder Koordinaten <input type="text" name="position" id="position"><input type="button" name="bShowLoc" value="Anzeigen und Umrechnen" onclick="ShowPos();"/><input type="button" value="automatisch ermitteln" onclick="GetLocation(GeoLocationData, GeoLocationFail)"/><br/>
Ziel <input type="text" name="direction" id="direction"><br/>
Fortbewegungsmittel <select name="transportation" id="transportation" size="1" value="">
      <option>zu Fuß</option>
      <option>Straßenbahn</option>
    </select><br/>
Kommentar <input type="text" name="comment" id="comment"><br/>
<input type="button" onclick="formSubmit();" value="Absenden"><br/><br/>
<input type="text" value="1" id="wayGroup" class="gpinput"/><input type="button" value="Weg anzeigen" onclick ="ShowWay();"/>
</form>
<div style="display:block">Ausgabe: <div id="output"></div></div>
</div>
<div id="right">
<input type="button" value="alle Kommentare" onclick="ShowAllComments();"/>
<input type="button" value="nur neueste" onclick="ShowLastComments();"/>
<input type="button" value="alle von" onclick="ShowCommentsBy();"/> <input type="text" width="3" id="groupComm" class="gpinput"/>
<input type="button" value="Statistik" onclick="ShowStatistics();"/>
<div id="comments">
</div>
</div>
</div>
</div>
</body>
</html>