<?php
 include('Class/Authentication.php');
 require_once '/etc/auth/cnmc_auth.env';

 $input = json_decode(file_get_contents('php://input'), true);
 $app_auth = $input['auth'];
//  echo $auth;
 if(CNMC_AUTH==$app_auth) {
  $auth = new userAuth();
  $circle_administrators = $auth->getCircleAdminstrators();
  echo json_encode(array("result"=>"true", "data"=>$circle_administrators, "error"=>""));

 } else {
  echo json_encode(array("result"=>"false", "data"=>"", "error"=>"There was an issue with the App Uninstall and install again as app is not properly installed"));
 }
?>