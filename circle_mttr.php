<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$ym = filter_input(INPUT_POST,"ym");
$mttr = $btsdown->circleMttr($ym);
echo json_encode($mttr);
?>
