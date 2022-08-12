<?php
    /* SSA wise Outsorced down */
    $input = json_decode(file_get_contents('php://input'), true);
    $circle_id=$input['circle_id'];
    $ssa_id=$input['ssa_id']; 
    include('classes/btsDown.php');
    $btsdown = new BtsDown();
    $outsource_down = $btsdown->downDetailsOutsourced($circle_id, $ssa_id);
    echo json_encode($outsource_down);
?>
