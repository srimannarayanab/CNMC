<?php
include('classes/authentication.php');

$msisdn = filter_input(INPUT_POST, 'msisdn');
$email = filter_input(INPUT_POST, 'email');
$password = filter_input(INPUT_POST, 'password');


$auth = new userAuth();
$forgot = $auth->forgotPassword($msisdn, $email, $password);
echo json_encode($forgot);
?>