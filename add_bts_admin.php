<?php
include('classes/myBts.php');
$bts_id = filter_input(INPUT_POST, "bts_id");
$msisdn1 = filter_input(INPUT_POST, "msisdn1");
$msisdn2 = filter_input(INPUT_POST, "msisdn2");
$msisdn3 = filter_input(INPUT_POST, "msisdn3");
$msisdn = array($msisdn1, $msisdn2, $msisdn3);
$mybts = new myBts();
foreach($msisdn as $k){
  if($k !=''){
     $addbts = $mybts->addBts($bts_id, $k);
  }
}
echo json_encode($addbts);
?>
