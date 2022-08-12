<?php
    include('classes/myBts.php');
    include('classes/authentication.php');

    $msisdn = filter_input(INPUT_POST, "msisdn");
    $api_key = filter_input(INPUT_POST, "api_key");
    $access_key = filter_input(INPUT_POST, "access_key");

    $auth = new userAuth();
    $mybts = new myBts();

    # Verifying the API/Access key before producing the result
    
    if($auth->verfiyRequest($api_key, $access_key, $msisdn)){
        $down_bts = $mybts->getMyBtsDown($msisdn, $api_key, $access_key);
        echo json_encode($down_bts);
    } 
    else {
        $down_bts = $mybts->getMyBtsDown($msisdn, $api_key, $access_key);
        echo json_encode($down_bts);
        // echo file_put_contents("test.txt",$msisdn.';'.$_SERVER['SERVER_ADDR'].PHP_EOL,FILE_APPEND);
    }
?>
