<?php
/* Site type wise Outage */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$sitetype_down = $btsdown->btsDownSiteTypeWIse();
echo json_encode($sitetype_down);
?>