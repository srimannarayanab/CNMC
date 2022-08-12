<?php
/* Circle wise technology down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$tech_wise_down = $btsdown->techWise();
echo json_encode($tech_wise_down);
?>
