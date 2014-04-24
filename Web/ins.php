<?php
   include('config.php');
   $db = new syDB();
   if ($db->Insert(htmlspecialchars($_POST["group"]),htmlspecialchars($_POST["position"]),htmlspecialchars($_POST["address"]),htmlspecialchars($_POST["direction"]),htmlspecialchars($_POST["transportation"]), htmlspecialchars($_POST["comment"])))
      echo "OK";
?>