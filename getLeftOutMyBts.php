<?php
include('classes/myBts.php');
$circle_id = filter_input(INPUT_POST, "circle_id");

$mybts = new myBts();
$leftout_bts = $mybts->getLeftOutMyBts($circle_id);

echo json_encode($leftout_bts);
?>
