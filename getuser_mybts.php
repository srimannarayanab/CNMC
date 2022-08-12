<?php
    include('classes/myBts.php');
    $user_privs = filter_input(INPUT_POST, "user_privs");
    $circle_id = filter_input(INPUT_POST, "circle_id");

    $mybts = new myBts();
    $user_counts = $mybts->getUserMyBtsCount($user_privs, $circle_id);

    echo json_encode($user_counts);
?>

