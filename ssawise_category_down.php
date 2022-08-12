<?php
/* Circle wise category down */
include('classes/btsDown.php');
$circle_id = filter_input(INPUT_POST,"circle_id");
$btsdown = new BtsDown();
// $cat_wise_down = $btsdown->btsDownCategoryWiseSSA($circle_id);
if($circle_id=='HRR' or $circle_id =='UWW'){
    $circle_1 = $btsdown->btsDownCategoryWiseSSA($circle_id);
    $circle_dl = $btsdown->btsDownCategoryWiseSSA('DL');
    $dummy_array = array();
    array_push($dummy_array,array("ssaid"=> "",
        "sc"=>"",
        "cri"=> "",
        "imp"=> "",
        "nor"=> "",
        "Total"=> "",
        "sc_cnt"=> "",
        "c_cnt"=> ""
    ));
    $cat_wise_down = array_merge($circle_1, $dummy_array, $circle_dl);
    // print_r($circle_dl);

} else {
    $cat_wise_down = $btsdown->btsDownCategoryWiseSSA($circle_id);
}  
echo json_encode($cat_wise_down);
?>