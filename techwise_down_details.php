<?php
    /* Techwise Down details */
    $_POST = json_decode(file_get_contents('php://input'), true);
    $circle_id=$_POST['circle_id'];
    $ssa_id=$_POST['ssa_id'];
    $bts_type=$_POST['bts_type'];
    include('classes/btsDown.php');
    $btsdown = new BtsDown();
    $techwise_down_details = $btsdown->techWisedownDetails($circle_id, $ssa_id, $bts_type);
    echo json_encode($techwise_down_details);
?>
