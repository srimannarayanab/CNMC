<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$partial_down = $btsdown->btsPartialDown();
echo json_encode($partial_down);
?>