<?php
/* Circle wise category down */
include('classes/btsDown.php');
$btsdown = new BtsDown();
$cat_wise_down = $btsdown->btsDownCategoryWise();
echo json_encode($cat_wise_down);
?>