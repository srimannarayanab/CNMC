<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$locked_sites = $btsdown->circleLockedSites();
echo json_encode($locked_sites);
?>
