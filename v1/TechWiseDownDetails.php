<?php
    /* Circle wise category down */
    include('Class/BtsDown.php');
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $circle_id = $input["circle_id"];
    $ssa_id = $input["ssa_id"];
    $bts_type = $input["bts_type"];
    $ssa_id = $ssa_id=="Total" ? "%" : $ssa_id; 
    
    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn, $web_token)){
        $btsdown = new BtsDown();
        $result = $btsdown->techWisedownDetails($circle_id, $ssa_id, $bts_type);
        $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>