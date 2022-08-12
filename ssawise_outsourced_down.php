<?php
    /* SSA wise Outsorced down */
    $input = json_decode(file_get_contents('php://input'), true);
    $circle_id=$input['circle_id']; 
    include('classes/btsDown.php');
    $btsdown = new BtsDown();
    $outsource_down = $btsdown->btsdownOutsourcedSSAWise($circle_id);
    echo json_encode($outsource_down);
?>
