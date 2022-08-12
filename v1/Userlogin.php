<?php
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);
    
    $username = $input['username'];
    $password = $input['password'];
    $version_no = $input['version_no'];
    $fmcid = $input['firebase_id'];

    # Get header paarameters    
    $api_key = $_SERVER['HTTP_X_API_KEY'];
    $api_username = $_SERVER['PHP_AUTH_USER'];
    $api_password = $_SERVER['PHP_AUTH_PW'];
    $auth = new userAuth();

    #verfiy api key
    if($auth->verifyAPIKey($api_key, $api_username, $api_password)){
        $verifyUser = $auth->verifyUserDetails($username, $password, $version_no, $fmcid);
        echo json_encode($verifyUser);
    } else {
        $res = array("result"=>'false', "remarks"=>"UnAuthorized access", "error"=>$_SERVER['HTTP_AUTHORIZATION']);
        echo json_encode($res);
    }
    
?>