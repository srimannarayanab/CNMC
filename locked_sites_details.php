<?php
include('classes/btsDown.php');
$bts = new BtsDown();
$circle_id = filter_input(INPUT_POST, "circle_id");
$ssa_id = filter_input(INPUT_POST, "ssa_id");
$vendor_id = filter_input(INPUT_POST, "vendor_id");
$locked_sites = $bts->lockedSitesDetails($circle_id, $ssa_id, $vendor_id);
echo json_encode($locked_sites);
?>
