<?php
include('classes/btsDown.php');
// $bts_id='1070663';
$circle_id = filter_input(INPUT_POST, "circle_id");
$bts = new BtsDown();
$locked_sites = $bts->ssaLockedSites($circle_id);
echo json_encode($locked_sites);
?>traffic = $trf->get3GTraffic();
$erl_4g_traffic = $trf->get4GTraffic();
//print_r($erl_3g_traffic);
// cumulative add and get circle wise
$x = array();
foreach($erl_2g_traffic as $k){
	$circle = $k['circle_id'];
	$ssa = $k['ssa_id'];
	$erl = $k['cell_traffic'];
	$data_vol = $k['data_vol'];
	if($circle==$circle_id) {
		if(array_key_exists($ssa, $x)){
			$x[$ssa]['erl'] +=$erl;
			$x[$ssa]['data'] +=$data_vol;
			
		} else {
			$x[$ssa]['erl'] = $erl;
			$x[$ssa]['data'] = $data_vol;
		}
	}
}

foreach($erl_3g_traffic as $k){
	$circle = $k['circle_id'];
	$ssa = $k['ssa_id'];
	$erl = $k['cell_traffic'];
	$data_vol = $k['data_vol'];
	if($circle == $circle_id) {
		if(array_key_exists($ssa, $x)){
			$x[$ssa]['erl'] +=$erl;
			$x[$ssa]['data'] +=$data_vol;
		} else {
			$x[$ssa]['erl'] = $erl;
			$x[$ssa]['data'] = $data_vol;
		}
	}
}

foreach($erl_4g_traffic as $k){
	$circle = $k['circle_id'];
	$ssa = $k['ssa_id'];
	$erl = $k['cell_traffic'];
	$data_vol = $k['data_vol'];
	if($circle == $circle_id) {
		if(array_key_exists($ssa, $x)){
			$x[$ssa]['erl'] +=$erl;
			$x[$ssa]['data'] +=$data_vol;
		} else {
			$x[$ssa]['erl'] = $erl;
			$x[$ssa]['data'] = $data_vol;
		}
	}
}

$output = array();
foreach($x as $k=>$v) {
	array_push($output, array("ssa_id"=>$k, "erl"=>$v['erl'], "data_vol"=>$v["data"]));
}

echo json_encode($output);
?>
