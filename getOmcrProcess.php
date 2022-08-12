<?php
    include('classes/btsDown.php');
    $btsdown = new BtsDown();
    $input = json_decode(file_get_contents('php://input'), true);
    $circle_id=$input['circle_id'];
    $omcr_process = $btsdown->getOMCRProcess($circle_id);
    echo json_encode($omcr_process);

?>
