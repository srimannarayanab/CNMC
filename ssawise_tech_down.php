<?php
    /* Circle wise technology down */
    $_POST = json_decode(file_get_contents('php://input'), true);
    $circle_id=$_POST['circle_id'];
    include('classes/btsDown.php');
    $btsdown = new BtsDown();
    // if Circle is Haryana the Haryana Team should get the Delhi Sites also

    if($circle_id=='HRR' or $circle_id =='UWW'){
        $circle_1 = $btsdown->techWiseCircle($circle_id);
        $circle_dl = $btsdown->techWiseCircle("DL");
        $dummy_array = array();
        array_push($dummy_array,array("ssa_id"=> "",
            "circle_id"=>"",
            "bts_2g_cnt"=> "",
            "bts_3g_cnt"=> "",
            "bts_4g_cnt"=> "",
            "bts_2g_down_cnt"=> "",
            "bts_3g_down_cnt"=> "",
            "bts_4g_down_cnt"=> "",
            "down_cnt"=> "",
            "total_cnt"=> "",
            "perc"=> ""
        ));
        $tech_wise_down = array_merge($circle_1, $dummy_array, $circle_dl);
        // print_r($circle_dl);

    } else {
        $tech_wise_down = $btsdown->techWiseCircle($circle_id);
    }  
    echo json_encode($tech_wise_down);
?>
