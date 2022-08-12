<?php
/* Circle wise category down */
include('classes/availability.php');
$avail = new Availability();
$ym = filter_input(INPUT_POST,"ym");
//$ym ="202004";
$dur = $avail->catWiseAvailability($ym);

//var_dump($dur);

$x = array(); # Circle wise each cat wise data
$t = array(); # Circle wise Total Duration/Count
$p = array(); # Parameter wise Total Duration/Count
// Circle wise category wise duration
foreach($dur as $k){
	$avail = round(100 - (100*$k['dur'])/(24*60*$k['days']*$k['site_count']),2);
	$x[$k['circle_id']][$k['param_type']] = $avail;
	if(array_key_exists($k['circle_id'], $t)){
		$t[$k['circle_id']]['site_count'] += $k['site_count'];
		$t[$k['circle_id']]['dur'] +=$k['dur'];
	} else {
		$t[$k['circle_id']]['site_count'] = $k['site_count'];
		$t[$k['circle_id']]['dur'] = $k['dur'];
		$t[$k['circle_id']]['days'] = $k['days'];
	}
// Paramter wise 

    if(array_key_exists($k['param_type'], $p)){
		$p[$k['param_type']]['dur'] += $k['dur'];
		$p[$k['param_type']]['site_count'] +=$k['site_count'];
	} else {
		$p[$k['param_type']]['dur'] = $k['dur'];
		$p[$k['param_type']]['site_count'] =$k['site_count'];
		$p[$k['param_type']]['days'] = $k['days'];
	}
}

#print_r($p);

# Circle wise total Duration
$output = array();
foreach($x as $k3 => $l){
	$circle = $k3;
	$total_dur=$t[$circle]['dur'];
	$total_sites = $t[$circle]['site_count'];
	$total_days = $t[$circle]['days'];
	$t_avail = round(100 - (100*$total_dur)/(24*60*$total_days*$total_sites) ,2 );
	$l['TOT'] = $t_avail;
	$l['circle_id'] = $circle;
	array_push($output, $l);
}
$at = array_column($output, 'TOT');
array_multisort($at, SORT_DESC, $output);


# CategoryWise Total Duration/ Avail
$total = array("circle_id"=>'TOT');
foreach($p as $k2=>$v2){
	$avail = round(100 - (100*$v2['dur'])/(24*60*$v2['days']*$v2['site_count']),2);
	$total[$k2]=$avail;
}

# All Category Avail

$total_dur = array_sum(array_column($p,'dur'));
$total_sitecount = array_sum(array_column($p, 'site_count'));
$total_days = $p['SUPER_CRITICAL']['days'];
$total_avail = round(100 - (100 * $total_dur)/(24*60*$total_days*$total_sitecount), 2);
$total['TOT'] = $total_avail;

array_push($output, $total);

$categories =array('SUPER_CRITICAL','CRITICAL','IMPORTANT','NORMAL','TOT');
$final_output = array();
foreach($output as $o){
	foreach($categories as $c){
		if(array_key_exists($c, $o)){
			// $o[$c] ="100";
		} else {
			$o[$c] = "100";
		}
	}
	array_push($final_output, $o);
}

echo json_encode($output);
?>
