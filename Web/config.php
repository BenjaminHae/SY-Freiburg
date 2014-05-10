<?php
class syDB{
	private $host, $user, $pwd;

	private $connection;
	function __construct() {
		include('private.php');
		$this->connection=mysql_connect($this->host, $this->user, $this->pwd) or die ("Verbindungsversuch fehlgeschlagen");
		mysql_select_db($this->db, $this->connection) or die("Konnte die Datenbank nicht waehlen.");
	}
	public function Insert($gp, $pos, $add, $dir, $trans, $comm)
	{
		settype($gp, 'integer');
		if ($gp<=0)
		   die("Keine Gruppennummer angegeben");
		if ($gp<10)
			die("Mister X kann nicht direkt gesetzt werden");
		if (($gp>690) and($gp<700))
			$gp -= 690;
		$pos = mysql_real_escape_string(trim($pos));
		$add = mysql_real_escape_string(trim($add));
		//if ($pos == "")
		   //die("Kein Standort angegeben");
		$dir = mysql_real_escape_string(trim($dir));
		$trans = mysql_real_escape_string(trim($trans));
		//if(($gp<10) and ($trans==""))
		//   die("Kein Fortbewegungsmittel angegeben");
		$comm = mysql_real_escape_string(trim($comm));
		$sql = "INSERT INTO scotland_yard VALUES ($gp,'$pos','$add','$dir','$trans','$comm', DEFAULT);";
		return mysql_query($sql, $this->connection) or die("Konnte den Datensatz nicht eintragen.");
	}
	public function GetGroupPosition($gp)
	{
		settype($gp, 'integer');
		$sql = "SELECT * FROM scotland_yard WHERE scotland_yard.group = $gp AND NOT scotland_yard.position = '' ORDER BY timestamp DESC LIMIT 1;";
		$query = mysql_query($sql, $this->connection);
		return mysql_fetch_array($query);
	}
	public function GetGroupMovement($gp, $limit=0)
	{
		settype($gp, 'integer');
		settype($limit, 'integer');
		$limString="";
		if ($limit>0)
		{
		   $limString=" LIMIT ".$limit;
		}
		$sql = "SELECT * FROM scotland_yard WHERE scotland_yard.group = $gp AND NOT scotland_yard.position = '' ORDER BY timestamp DESC$limString;";
		$query = mysql_query($sql, $this->connection);
			while($r=mysql_fetch_array($query))
			{
				$res[]=$r;
			}
		return $res;
	}
	public function GetGroupIDs()
	{
		$sql = "SELECT DISTINCT scotland_yard.group FROM scotland_yard ORDER BY scotland_yard.group;";
		$query = mysql_query($sql, $this->connection);
		while($r=mysql_fetch_array($query))
		{
			$res[]=$r[0];
		}
		return $res;
	}
	public function GetCommentsBy($gp, $limit=0)
	{
		settype($gp,'integer');
		settype($limit, 'integer');
		$limString="";
		if ($limit>0)
		{
		   $limString=" LIMIT ".$limit;
		}
		$sql = "SELECT * FROM scotland_yard WHERE scotland_yard.group = $gp AND NOT scotland_yard.comment='' ORDER BY timestamp DESC$limString;";
		$query = mysql_query($sql, $this->connection);
		while($r=mysql_fetch_array($query))
		{
			$res[]=$r;
		}
		return $res;
	}
	public function GetAllComments($limit=0)
	{
		settype($limit, 'integer');
		$limString="";
		if ($limit>0)
		{
		   $limString=" LIMIT ".$limit;
		}
		$sql = "SELECT * FROM scotland_yard WHERE NOT scotland_yard.comment='' ORDER BY timestamp DESC$limString;";
		$query = mysql_query($sql, $this->connection);
		while($r=mysql_fetch_array($query))
		{
			$res[]=$r;
		}
		return $res;
	}
}
?>