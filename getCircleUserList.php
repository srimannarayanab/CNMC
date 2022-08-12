<?php
include('classes/myBts.php');
$circle_id = filter_input(INPUT_POST, "circle_id");

$mybts = new myBts();
$userlist = $mybts->getCircleUsersList($circle_id);
array_walk_recursive($userlist,function(&$item){$item=strval($item);});
echo json_encode($userlist);
?>