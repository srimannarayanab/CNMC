<?php
include('classes/trafficDetails.php');
$kpi_date = filter_input(INPUT_POST, "kpi_date");
$traffic = new traffic();
$circle_traffic = $traffic->circleTraffic($kpi_date);
echo json_encode($circle_traffic);
?>
