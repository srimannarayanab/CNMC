<?php
include('classes/authentication.php');

$username = filter_input(INPUT_POST,"username");
$password = filter_input(INPUT_POST,"password");
$version_no = filter_input(INPUT_POST,"version_no");
$firebase_id = filter_input(INPUT_POST, "firebase_id");

$auth = new userAuth();
$verifyUser = $auth->verifyUserDetails($username, $password, $version_no,$firebase_id);
echo json_encode($verifyUser);

?>