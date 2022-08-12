<?php
    include('classes/authentication.php');
    $auth = new userAuth();
    $circle_id = $_POST['circle_id'];
    $msisdn = $_POST['msisdn'];

    $userDetails = $auth->getUserDetails($msisdn, $circle_id);
    echo json_encode($userDetails);

?>