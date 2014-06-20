var MisterXMarkers = new Array();
var GroupMarkers = new Array();
var GroupWays = new Array();
function GetAJAX(call, callback)
{
	var xmlhttp=new XMLHttpRequest();
	xmlhttp.open("GET","ajax.php?AJAX="+call,true);
	xmlhttp.onreadystatechange=function (){
		if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{	
			callback(xmlhttp.responseText);
		}
	}
	xmlhttp.send();
}
function PostAJAX(page, params, callback)
{
	var xmlhttp=new XMLHttpRequest();
	xmlhttp.open("POST",page,true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.onreadystatechange=function (){
		if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{
			callback(xmlhttp.responseText);
		}
	};
	xmlhttp.send(params);
}
function SetOutput(str)
{
	document.getElementById("output").innerHTML=str;
}
function getPos()
{
	document.getElementById("output").innerHTML="lade...";
	var gp = document.getElementById("tG").value;
	GetAJAX("pos&group="+gp, SetOutput);
}
function getWay()
{
	document.getElementById("output").innerHTML="lade...";
	gp = document.getElementById("tG").value;
	GetAJAX("way&group="+gp, SetOutput);
}
function formSubmit()
{
	var post = '';
	var myStringArray = new Array("group","address","position","direction","transportation","comment");
	var group = document.getElementById("group").value;
	for(var i = 0; i<myStringArray.length; i++)
	{
		var s = myStringArray[i];
		post += s+'='+encodeURIComponent(document.getElementById(s).value)+'&';
	}
	post=post.substr(0, post.length-1);
	PostAJAX('ins.php',post,SetOutput);
	document.getElementById("insLoc").reset();
	document.getElementById("insLoc").style.display="none";
	document.getElementById("group").value=group;
}
function ShowAdr()
{
	codeAddress(document.getElementById('address').value+" ,Freiburg");
}
function ShowPos()
{
	codeLatLng(document.getElementById('position').value);
}
function ShowMisterX()
{
	GetAJAX("xgroups", MisterXCallback);
	ShowXMove();
}
function ShowHGroups()
{
	GetAJAX("hgroups", HGroupsCallback);
}
function MisterXCallback(str)
{
	removeMarkers(MisterXMarkers);
	var groups = str.split('<br/>');
	for (var i=0; i<groups.length; i++)
	{
		var info = groups[i].split("\r\n");
		if (info.length!=8)
			break;
		if (info[1]!="")//LatLong angegeben
		{
			var marker = addMarkerByPos("MisterX "+info[0], info[1]);
			MisterXMarkers.push(marker);
			var boxstr="MisterX "+info[0]+"<br/>"+info[6]+"<br/>";
			if (info[2].trim()!="")
			{
				boxstr +=info[2]+"<br/>";
			}
			if (info[4].trim()!="")
			{
				boxstr +="<b>"+info[4]+"</b><br/>";
			}
			if (info[3].trim()!="")
			{
				boxstr +="<b>Richtung: </b>"+info[3]+"<br/>";
			}
			if (info[5].trim()!="")
			{
				boxstr +="<em>"+info[5]+"</em>";
			}
			addInfoBox(marker, boxstr);
		}
	}
}
function HGroupsCallback(str)
{
	removeMarkers(GroupMarkers);
	var groups = str.split('<br/>');
	for (var i=0; i<groups.length; i++)
	{
		var info = groups[i].split("\r\n");
		if (info.length!=8)
			break;
		if (info[1]!="")//LatLong angegeben
		{
			var marker = addMarkerByPos("Verfolger "+info[0], info[1], true);
			GroupMarkers.push(marker);
			var boxstr="Verfolger "+info[0]+"<br/>"+info[6]+"<br/>";
			if (info[2].trim()!="")
			{
				boxstr +=info[2]+"<br/>";
			}
			if (info[4].trim()!="")
			{
				boxstr +="<b>"+info[4]+"</b><br/>";
			}
			if (info[3].trim()!="")
			{
				boxstr +="<b>Richtung: </b>"+info[3]+"<br/>";
			}
			if (info[5].trim()!="")
			{
				boxstr +="<em>"+info[5]+"</em>";
			}
			addInfoBox(marker, boxstr);
		}
	}
}
function ShowXMove()
{
	removeMarkers(GroupWays);
	GetAJAX("exX", MrXMoveCallback);
}
function MrXMoveCallback(str)
{
	var mrX = str.split('\r\n');
	for (var i=0; i<mrX.length; i++)
	{
		if (mrX[i]!="")
		{
			GetAJAX("lastMovement&group="+mrX[i],DrawWayCallback);
		}
	}
}
function HideGroups()
{
	removeMarkers(GroupMarkers);
	removeMarkers(MisterXMarkers);
	removeMarkers(GroupWays);
}
function DrawWayCallback(str)
{
	var waypoints = str.split('<br/>');
	var points = new Array();
	for (var i=0; i<waypoints.length; i++)
	{
		var info = waypoints[i].split("\r\n");
		if (info.length!=8)
		{
			break;
		}
		if (info[1]!="")//LatLong angegeben
		{
			points.push(parseLatLong(info[1]));
		}
		else
		{
		}
	}
	GroupWays.push(drawWay(points,'#000000'));
}
function removeMarkers(arr)
{
	for (i=0; i<arr.length; i++)
	{
		removeMarker(arr[i]);
	}
	arr = new Array();
}
function ShowInput()
{
	document.getElementById("insLoc").style.display="block";
}
function ShowWay()
{
	removeMarkers(GroupWays);	
	GetAJAX("way&group="+document.getElementById("wayGroup").value, DrawWayCallback);
}
function GeoLocationData(position)
{
	document.getElementById("position").value=position;
	document.getElementById('position').value= document.getElementById('position').value.substr(1,document.getElementById('position').value.length-2);
}
function GeoLocationFail(active)
{
	if (active)
	{
		document.getElementById("position").value="Geolocation fehlgeschlagen";
	}
	else
		document.getElementById("position").value="Geolocation nicht verfügbar";
}
function ShowAllComments()
{
	comments = document.getElementById("comments");
	comments.innerHTML = "";
	GetAJAX("allComments", DisplayComments);
}
function DisplayComments(str)
{
	comments = document.getElementById("comments");
	var waypoints = str.split('<br/>');
	for (var i=0; i<waypoints.length; i++)
	{
		var info = waypoints[i].split("\r\n");
		if (info.length!=8)
		{
			break;
		}
		comments.innerHTML +='<div><b>'+info[0]+'</b> <em>'+info[6]+'</em><br/><div class="comment">'+info[5]+'</div>';
	}
}
function ShowLastComments()
{
	comments = document.getElementById("comments");
	comments.innerHTML = "";
	GetAJAX("exX", LastCommentCallback);
}
function LastCommentCallback(str)
{
	var mrX = str.split('\r\n');
	for (var i=0; i<mrX.length; i++)
	{
		if (mrX[i]!="")
		{
			GetAJAX("commentsBy&group="+mrX[i]+"&max=1",DisplayComments);
		}
	}
}
function ShowCommentsBy()
{
	comments = document.getElementById("comments");
	comments.innerHTML = "";
	gp = document.getElementById("groupComm").value;
	GetAJAX("commentsBy&group="+gp,DisplayComments);
}
function ShowStatistics()
{
	comments = document.getElementById("comments");
	comments.innerHTML = "";
	gp = document.getElementById("groupComm").value;
	if (parseInt(gp)>10)
		GetAJAX("caughtX&group="+gp,DisplayStatisticC);
	else
		GetAJAX("caughtBy&group="+gp,DisplayStatisticX);
}
function DisplayStatisticC(str)
{
	comments = document.getElementById("comments");
	comments.innerHTML = "hat die Gruppen ";
	var mrX = str.split('\r\n');
	for (var i=0; i<mrX.length; i++)
	{
		if (mrX[i]!="")
		{
			comments.innerHTML += mrX[i]+", ";
		}
	}
	comments.innerHTML+=" gefangen";
}
function DisplayStatisticX(str)
{
	comments = document.getElementById("comments");
	var mrX = str.split('\r\n');
	for (var i=0; i<mrX.length; i++)
	{
		if (mrX[i]!="")
		{
			comments.innerHTML += mrX[i]+"<br/>";
		}
	}
}