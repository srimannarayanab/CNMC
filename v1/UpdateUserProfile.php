<?php
    /* Update user Levels */
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $username = $input['username'];
    $name = $input['name'];
    $desg = $input['desg'];
    $hrms = $input['hrms'];
    $circle = $input['circle'];
    $email = $input['email'];
    $password = $input['password'];
    $lvl = $input['lvl'];
    $lvl2 = $input['lvl2'];
    $lvl3 = $input['lvl3'];
    $ssaname = $input['ssaname'];

    // if(is_null($username)){
    //     $username=$msisdn;
    // }
    // echo $web_token;
    // echo $msisdn;

    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn, $web_token)){
        $auth = new userAuth();
        $result = $auth->updateProfile($name, $desg, $hrms, $email, $circle, $password, $lvl, $lvl2, $lvl3, $username, $ssaname);
        // $output = array("result"=>"true", "data"=>"", "error"=>"");
        echo json_encode($result);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>