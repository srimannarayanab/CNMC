<?php
    include('classes/authentication.php');
    $auth = new userAuth();

    $desg = $auth->getUserDesignations();
    echo json_encode($desg);
?>