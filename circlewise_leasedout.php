<?php
/* Circle wise Durtaion wise down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$leased_out = $btsdown->btsdownLeasedOut();
echo json_encode($leased_out);
?>