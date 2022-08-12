<?php
    include('classes/authentication.php');
    $msisdn = filter_input(INPUT_POST, "msisdn");
    $auth = new userAuth();
    $user_details = $auth->activateUser($msisdn);
    echo json_encode($user_details);
?>
