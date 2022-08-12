<?php
    include('classes/authentication.php');
    $auth = new userAuth();
    $input = json_decode(file_get_contents('php://input'), true);
    $circle = $input['circle'];

    $ssas = $auth->getCircleSSAs($circle);
    echo json_encode($ssas);
?>
