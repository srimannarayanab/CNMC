<?php
    include('classes/authentication.php');
    $msisdn = filter_input(INPUT_POST, "msisdn");
    $auth = new userAuth();
    $del_user = $auth->deleteUser($msisdn);
    echo json_encode($del_user);
?>
