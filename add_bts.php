<?php
include('classes/myBts.php');
$msisdn = filter_input(INPUT_POST, "msisdn");
$ids = filter_input(INPUT_POST, "ids");
$btsids = json_decode($ids);

$mybts = new myBts();

foreach($btsids as $id) {
	$addbts = $mybts->addBts($id, $msisdn);
}
echo json_encode($addbts);
?>