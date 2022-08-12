<?php
/* Site type wise Outage */
include('classes/btsDown.php');
$circle_id=filter_input(INPUT_POST,"circle_id");
$btsdown = new BtsDown();
$sitetype_down = $btsdown->btsDownSiteTypeSSAWIse($circle_id);
echo json_encode($sitetype_down);
?>