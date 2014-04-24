<?php
include('config.php');
$db = new syDB();
function OutputGroupInfo($gpinfo)
{
	$s='';
	for ($i=0; $i<7;$i++)
	{
		$s.="$gpinfo[$i] \r\n";
	}
	return $s;
}
if($_GET["AJAX"]=="pos")
{
	echo OutputGroupInfo($_GET["group"],$db);
}
if($_GET["AJAX"]=="way")
{
	var_dump($db->GetGroupMovement($_GET["group"]));
}
if($_GET["AJAX"]=="xgroups")
{
	foreach($db->GetGroupIDs() as $gp)
	{
		if ($gp<10)
		{
			$gpinfo = $db->GetGroupPosition($gp);
			echo OutputGroupInfo($gpinfo);
			echo '<br/>';
		}
	}
}
//verfolgende Gruppen anzeigen
if($_GET["AJAX"]=="hgroups")
{
	foreach($db->GetGroupIDs() as $gp)
	{
		if ($gp>=10)
		{
			$gpinfo = $db->GetGroupPosition($gp);
			echo OutputGroupInfo($gpinfo);
			echo '<br/>';
		}
	}
}
if($_GET["AJAX"]=="lastMovement")
{
	foreach($db->GetGroupMovement($_GET["group"],2) as $gp)
	{
		echo OutputGroupInfo($gp);
		echo "<br/>";
	}
}
if($_GET["AJAX"]=="way")
{
	foreach($db->GetGroupMovement($_GET["group"]) as $gp)
	{
		echo OutputGroupInfo($gp);
		echo "<br/>";
	}
}
if($_GET["AJAX"]=="allComments")
{
	$max = 0;
	if (isset($_GET["max"]))
		$max = $_GET["max"];
	foreach($db->GetAllComments($max) as $gp)
	{
		echo OutputGroupInfo($gp);
		echo "<br/>";
	}
}
if($_GET["AJAX"]=="commentsBy")
{
	$max = 0;
	if (isset($_GET["max"]))
		$max = $_GET["max"];
	foreach($db->GetCommentsBy($_GET["group"],$max) as $gp)
	{
		echo OutputGroupInfo($gp);
		echo "<br/>";
	}
}
if($_GET["AJAX"]=="exX")
{
	foreach($db->GetGroupIDs() as $gp)
	{
		if ($gp<10)
		{
			echo $gp."\r\n";
		}
	}
}
?>