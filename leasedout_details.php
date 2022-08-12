<?php
/* Duration wise Bts Faults */
include('classes/btsDown.php');
$circle = filter_input(INPUT_POST,"circle");
$ssaname = filter_input(INPUT_POST,"ssaname");
$criteria = filter_input(INPUT_POST,"criteria"); 

$ssaname = $ssaname=="Total" ? "%" : $ssaname;

$btsdown = new BtsDown();
$details = $btsdown->downDetailsLeasedout($circle, $ssaname, $criteria);
echo json_encode($details);
?>