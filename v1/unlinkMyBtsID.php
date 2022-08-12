<?php
    /* Circle wise category down */
    include('Class/myBts.php');
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $mybts_id = $input['mybts_id'];
    

    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn,$web_token)){
        $mybts = new myBts();
        $output = $mybts->unlinkMyBtsID($mybts_id, $msisdn);
        // $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>