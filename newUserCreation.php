<?php
include('classes/authentication.php');

$name = filter_input(INPUT_POST, 'name');
$desg = filter_input(INPUT_POST, 'desg');
$hrms = filter_input(INPUT_POST, 'hrms');
$email = filter_input(INPUT_POST, 'email');
$msisdn = filter_input(INPUT_POST,'msisdn');
$password = filter_input(INPUT_POST,'password');
$circle = filter_input(INPUT_POST,'circle');
$access_key = filter_input(INPUT_POST,'access_key');

$auth = new userAuth();
$adduser = $auth->addUser($name, $desg, $hrms, $email, $msisdn, $password, $circle,$access_key);
echo json_encode($adduser);
?>
