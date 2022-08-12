<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$ym = filter_input(INPUT_POST,"ym");
$circle_id = filter_input(INPUT_POST,"circle_id");
$ssa_mttr = $btsdown->ssaMttr($ym, $circle_id);
echo json_encode($ssa_mttr);
?>
