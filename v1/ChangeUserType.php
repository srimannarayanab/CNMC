<?php
    /* Update user Levels */
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $username = $input['username'];
    $user_privs = $input['user_privs'];
    $circle_id = $input['circle_id'];

    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn,$web_token)){
        $auth = new userAuth();
        $result = $auth->modifyUserType($username, $user_privs, $circle_id);
        $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>