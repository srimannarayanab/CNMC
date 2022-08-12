<?php
    include('classes/authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);
    $auth = new userAuth();
    $updt_level = $auth->userLevelUpdate($msisdn, $lvl, $lvl2, $lvl3);
    echo json_encode($updt_level);
?>
