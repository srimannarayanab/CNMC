<?php
    include('classes/authentication.php');
    $msisdn = filter_input(INPUT_POST, "msisdn");
    $lvl = filter_input(INPUT_POST, "lvl");
    $lvl2 = filter_input(INPUT_POST, "lvl2");
    $lvl3 = filter_input(INPUT_POST, "lvl3");
    $auth = new userAuth();
    $updt_level = $auth->userLevelUpdate($msisdn, $lvl, $lvl2, $lvl3);
    echo json_encode($updt_level);
?>
