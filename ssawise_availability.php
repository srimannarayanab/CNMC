<?php
/* Circle wise category down */
include('classes/btsDown.php');
$circle_id = filter_input(INPUT_POST,"circle_id");
$btsdown = new BtsDown();
$circle_avail = $btsdown->ssaWiseCurrentAvailability($circle_id);
echo json_encode($circle_avail);
?>
