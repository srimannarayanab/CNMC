<?php
include('classes/authentication.php');
$name = filter_input(INPUT_POST,"name");
$desg = filter_input(INPUT_POST,"desg");
$hrms = filter_input(INPUT_POST,"hrms");
$msisdn = filter_input(INPUT_POST,"msisdn");
$email = filter_input(INPUT_POST,"email");
$circle = filter_input(INPUT_POST,"circle");
$update_profile = 'Y';
$password = filter_input(INPUT_POST,"password");

$auth = new userAuth();
$updt_details = $auth->updateUserDetails($name, $desg, $hrms, $msisdn, $email, $circle, $update_profile, $password);
echo json_encode($updt_details);
?>