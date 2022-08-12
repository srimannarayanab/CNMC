<?php
include('classes/myBts.php');
$ssa_id = filter_input(INPUT_POST, "ssa_id");

$mybts = new myBts();
$ssa_bts = $mybts->getBtsSSA($ssa_id);

echo json_encode($ssa_bts);
?>