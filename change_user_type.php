<?php
    include('classes/authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);
    $msisdn = $input['msisdn'];
    $user_privs = $input['user_privs'];
    $circle_id = $input['circle_id'];
    $auth = new userAuth();
    $chg_user_type = $auth->modifyUserType($msisdn, $user_privs, $circle_id);
    echo json_encode($chg_user_type);
?>
