<?php
 include('classes/authentication.php');
 $auth = new userAuth();
 $circle_administrators = $auth->getCircleAdminstrators();
 echo json_encode($circle_administrators);
?>
