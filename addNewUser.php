<?php
include('classes/authentication.php');

$name = filter_input(INPUT_POST, 'name');
$desg = filter_input(INPUT_POST, 'desg');
$hrms = filter_input(INPUT_POST, 'hrms');
$email = filter_input(INPUT_POST, 'email');
$msisdn = filter_input(INPUT_POST,'msisdn');
$password = filter_input(INPUT_POST,'password');
$circle = filter_input(INPUT_POST,'circle');
$ssaname = filter_input(INPUT_POST, 'ssaname');
$access_key = filter_input(INPUT_POST,'access_key');
$usertype = filter_input(INPUT_POST,'user_type');

$usertypes = array('Bsnl'=>'co', 'OutSourcing'=>'outSource');
$user_type = $usertypes[$usertype];

$auth = new userAuth();
$adduser = $auth->addNewUser($name, $desg, $hrms, $email, $msisdn, $password, $circle, $ssaname, $user_type, $access_key);
echo json_encode($adduser);
?>
