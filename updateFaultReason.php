<?php
/* Get the raw input data */

$input = json_decode(file_get_contents('php://input'), true);
$bts_id = $input['log_id'];
$bts_down_cause = $input['bts_down_cause'];
$added_by = $input['added_by'];


include('classes/btsDown.php');
$btsDown = new btsDown();
$fault_details = $btsDown->getBtsDownCause($bts_id);
#print_r($fault_details);
$update_reason = $btsDown->updateBtsDownFaultReason($bts_id, $fault_details['bts_down_cell_cnt'],
        $fault_details['bts_status_dt'], $fault_details['bts_status'] , $bts_down_cause, $added_by);
echo json_encode($update_reason);
?>
