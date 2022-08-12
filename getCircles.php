<?php
    include('classes/authentication.php');
    $auth = new userAuth();

    $circles = $auth->getCircles();
    echo json_encode($circles);
?>
