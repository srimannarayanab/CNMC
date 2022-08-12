<?php
    ini_set('display_errors', 1);
    ini_set('display_startup_errors', 1);
    error_reporting(E_ALL);
    /* ssa wise Leased down */
    include('Class/BtsDown.php');
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $bts_id = $input['log_id']; // It is same as bts id
    $bts_down_cause = $input['bts_down_cause'];
    
    
    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn, $web_token)){
        // print_r($input);
        $btsDown = new BtsDown();
        $fault_details = $btsDown->getBtsDownCause($bts_id);
        // echo $fault_details;
        $update_reason = $btsDown->updateBtsDownFaultReason($bts_id, $fault_details['bts_down_cell_cnt'],
        $fault_details['bts_status_dt'], $fault_details['bts_status'] , $bts_down_cause, $msisdn);
        echo json_encode($update_reason);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>