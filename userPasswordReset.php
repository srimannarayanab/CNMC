<?php
    include('classes/authentication.php');
    $auth = new userAuth();
    $msisdn = $_POST['msisdn'];

    $passwdreset = $auth->userPasswordReset($msisdn);
    echo json_encode($passwdreset);

?>