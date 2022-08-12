<?php
    /* Circle Traffic */
    include('Class/trafficDetails.php');
    include('Class/Authentication.php');
    $input = json_decode(file_get_contents('php://input'), true);

    $auth = new userAuth();
    # Get the Token
    $web_token = $_SERVER['HTTP_AUTHORIZATION'];
    $msisdn = $input['msisdn'];
    $kpi_date = $input['kpi_date'];

    # Verfiy the web token
    if($auth->verifyUserWebToken($msisdn,$web_token)){
        $traffic = new Traffic();
        $result = $traffic->circleTraffic($kpi_date);
        $output = array("result"=>"true", "data"=>$result, "error"=>"");
        echo json_encode($output);
    } else {
        echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
    }   
?>