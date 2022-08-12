<?php
    include('Class/TokenGeneration.php');
    $token = new TokenGeneration();
    $getToken = $token->getToken(256);
    echo $getToken;
?>