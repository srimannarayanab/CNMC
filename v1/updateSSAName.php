<?php
      /* Get Circles  */ 

 include('Class/Authentication.php');
 require_once '/etc/auth/cnmc_auth.env';

 $input = json_decode(file_get_contents('php://input'), true);
 $app_auth = $input['auth'];
 $username = $input['username'];
 $ssaname = $input['ssaname'];
 if(CNMC_AUTH==$app_auth) {
  $auth = new userAuth();
  $updt = $auth->updateUserSSAName($ssaname, $username);
  echo json_encode($updt);
  } else {
    echo json_encode(array("result"=>"fail", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
  } 
    
    
?>