<?php
require_once 'env.inc';

class Database {

	public function getConnection(){		
		$conn = new mysqli(SERVER, USERNAME, PASSWORD, DATABASE);
		if($conn->connect_error){
			die("Error failed to connect to MySQL: " . $conn->connect_error);
		} else {
			return $conn;
		}
    }
}
?>