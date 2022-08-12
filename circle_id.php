<?php
include('classes/authentication.php');
$auth = new userAuth();
$circle_id = $auth->getCircleDetails();
echo json_encode($circle_id);
?>