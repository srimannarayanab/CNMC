<?php
    /* Get Circles  */ 

 include('Class/Authentication.php');
 require_once '/etc/auth/cnmc_auth.env';

 $input = json_decode(file_get_contents('php://input'), true);
 $app_auth = $input['auth'];
 $name = $input['name'];
 $desg = $input['desg'];
 $hrms = $input['hrms'];
 $email = $input['email'];
 $msisdn = $input['msisdn'];
 $password = $input['password'];
 $circle =  $input['circle'];
 $ssaname = $input['ssaname']; 
 $access_key = $input['access_key'];
 $usertype = $input['user_type'];

 $usertypes = array('Bsnl'=>'Bsnl', 'OutSourcing'=>'outSource');
 $user_type = $usertypes[$usertype];
  
 if(CNMC_AUTH==$app_auth) {
  $auth = new userAuth();
  $adduser = $auth->addNewUser($name, $desg, $hrms, $email, $msisdn, $password, $circle, $ssaname, $user_type, $access_key);
  // $output = array("result"=>"true", "data"=>"", "error"=>"");
  echo json_encode($adduser);
  } else {
    echo json_encode(array("result"=>"false", "data"=>"", "error"=>$input));
  } 
?>
