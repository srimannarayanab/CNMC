<?php
include('classes/authentication.php');
$auth = new userAuth();
$ssa_ids = $auth->getSSADetails();
echo json_encode($ssa_ids);
?>