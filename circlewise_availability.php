<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$circle_avail = $btsdown->circleWiseCurrentAvailability();
echo json_encode($circle_avail);
?>