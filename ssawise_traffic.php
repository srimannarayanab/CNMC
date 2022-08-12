<?php
include('classes/trafficDetails.php');
$circle_id = filter_input(INPUT_POST, "circle_id");
$kpi_date = filter_input(INPUT_POST, "kpi_date");
$traffic = new traffic();
$ssa_traffic = $traffic->ssaTraffic($circle_id, $kpi_date);
echo json_encode($ssa_traffic);
?>
