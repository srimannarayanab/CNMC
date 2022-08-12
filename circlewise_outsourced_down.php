<?php
/* Circle wise Durtaion wise down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$outsource_down = $btsdown->btsdownOutsourcedWise();
echo json_encode($outsource_down);
?>
