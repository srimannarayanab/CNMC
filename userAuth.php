<?php
include('classes/authentication.php');
$imsi = filter_input(INPUT_POST, 'imsi');
$imei = filter_input(INPUT_POST, 'imei');
$imsi_1 = filter_input(INPUT_POST, 'imsi_1');
$imei_1 = filter_input(INPUT_POST, 'imei_1');
$firebase_id = filter_input(INPUT_POST,'token');
$access_key = filter_input(INPUT_POST,'access_key');
$msisdn_phone_state = filter_input(INPUT_POST, 'msisdn');


//Instaniate the Authnetication class
$auth = new userAuth();
$userdetails = $auth->getUserDetails($imsi);
//echo json_encode($userdetails);

//Verify already the user details are available or not
// result is true then details are available
if($userdetails['result']=='true'){
	$output = $auth->getUserDetails($imsi);
	
} else{ 
// User details not availbale to be added into the mysql table 
    $output = $auth->addUserDetails($imsi, $imei, $imsi_1, $imei_1, $firebase_id, $access_key, $msisdn_phone_state);
	echo json_encode($output);
//    If user added sucessfully
    if($adduser['result']=='true'){
        $ouptut = array('resp'=>'true', 'remarks'=>'User added');
    } else{
        $ouptput = array('resp'=>'false', 'remarks'=>$adduser['remarks']);
    }
}
echo json_encode($output);
?>

