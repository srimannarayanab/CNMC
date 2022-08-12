<?php
/* Circle wise Durtaion wise down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$cat_wise_down = $btsdown->btsDownDurationWise();
echo json_encode($cat_wise_down);
?>