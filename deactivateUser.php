<?php
    include('classes/authentication.php');
    $auth = new userAuth();
    $msisdn = $_POST['msisdn'];

    $deactivateUser = $auth->deactivateUser($msisdn);
    echo json_encode($deactivateUser);

?>