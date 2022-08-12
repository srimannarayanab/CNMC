<?php
/* Site type wise Outage */
include('classes/btsDown.php');
$circle_id=filter_input(INPUT_POST,"circle_id");
$btsdown = new BtsDown();
//$circle_id = 'KA';
$ssawise_leasedout = $btsdown->btsdownLeasedOutSSA($circle_id);
echo json_encode($ssawise_leasedout);
?>