<?php
	include('classes/authentication.php');
	$ssaname = filter_input(INPUT_POST, "ssaname");
	$username = filter_input(INPUT_POST, "username");

	$auth = new userAuth();
	$updt_ssaname = $auth->updateUserSSAName($ssaname, $username);
	echo json_encode($updt_ssaname);
?>
