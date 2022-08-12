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
    $criteria = $input["criteria"];
    $ssa_id = $ssa_id=="Total" ? "%" : $ssa_id; 
    // Here Criteria is bts type
    
    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn, $web_token)){
        $btsdown = new BtsDown();
        $result = $btsdown->downDetailsPartial($circle_id, $ssa_id, $criteria);
        $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>