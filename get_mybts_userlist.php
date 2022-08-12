<?php
    include('classes/myBts.php');
    $circle = filter_input(INPUT_POST, "circle");

    $mybts = new myBts();
    $myBtslist = $mybts->getCircleMyBtsUsersList($circle);
    array_walk_recursive($myBtslist,function(&$item){$item=strval($item);});
    echo json_encode($myBtslist);
?>