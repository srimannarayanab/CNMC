<?php
include('classes/myBts.php');
$msisdn = filter_input(INPUT_POST, "msisdn");
// print($msisdn);

$mybts = new myBts();
$bts = $mybts->viewBts($msisdn);
echo json_encode($bts);
?>
