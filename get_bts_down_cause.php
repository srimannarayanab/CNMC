<?php
include('classes/btsDown.php');
$bts_id = filter_input(INPUT_POST,"bts_id");
// $bts_id='1070663';
$bts = new BtsDown();
$site_details = $bts->getBtsDownCause($bts_id);
echo json_encode($site_details);
?>
