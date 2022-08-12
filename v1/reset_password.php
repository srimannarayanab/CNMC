<?php
    /* Get Circles  */ 

 include('Class/Authentication.php');
 require_once '/etc/auth/cnmc_auth.env';

  $input = json_decode(file_get_contents('php://input'), true);
  $app_auth = $input['auth'];
  $msisdn =  $input['msisdn'];
  $email = $input['email'];
  $password = $input['password'];
 if(CNMC_AUTH==$app_auth) {
  $auth = new userAuth();
  $output = $auth->forgotPassword($msisdn, $email, $password);

  echo json_encode($output);
  } else {
    echo json_encode(array("result"=>"false", "data"=>"", "error"=>"Session Expired\nLogin once again...!"));
  } 
?>
