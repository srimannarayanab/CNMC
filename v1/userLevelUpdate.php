<?php
    /* Update user Levels */
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $username = $input['username'];
    $desg = $input['desg'];
    $lvl = $input['lvl'];
    $lvl2 = $input['lvl2'];
    $lvl3 = $input['lvl3'];

    if(is_null($username)){
        $username=$msisdn;
    }

    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn, $web_token)){
        $auth = new userAuth();
        $result = $auth->userLevelUpdate($username, $desg, $lvl, $lvl2, $lvl3);
        $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>