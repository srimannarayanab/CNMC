<?php
require_once __DIR__.'/../config/env.inc';

class Availability {
	function __construct(){
		$this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysql_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysql_error());
	}
	
	function catWiseAvailability($ym) {
//		$sql = "SELECT aa.circle_id,aa.ssa_id, param_type, SUM(dur) dur, DATE_FORMAT(outage_dt,'%Y%m') ym  FROM (
//				SELECT a.*, DATE_SUB(insert_dt, INTERVAL 1 DAY) outage_dt FROM ntmsdb.nw_ssa_wise_availbility a WHERE param='site_cat' ) aa 
//				WHERE DATE_FORMAT(outage_dt,'%Y%m')='$ym'
//				GROUP BY circle_id, ssa_id, param_type";

		$sql = "SELECT circle_id,param_type,SUM(dur) dur, SUM(site_count) site_count, MAX(days) days FROM (
				SELECT circle_id, ssa_id, param_type,SUM(dur) dur, MAX(total_site_count) site_count, MAX(insert_dt) INSERT_dt, 
				DAY(MAX(insert_dt)) days FROM 
				cnmcmob.nw_ssa_wise_availability WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym' AND param='site_cat'
				GROUP BY circle_id, ssa_id,param_type) aa 
				GROUP BY circle_id, param_type";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function catWiseSiteCount($ym) {
		$sql = "SSELECT circle_id,ssa_id,param_type,total_site_count FROM cnmcmob.nw_ssa_wise_availability
				WHERE date_format(insert_dt,'%Y%m')='$ym' AND param='site_cat'
				AND insert_dt = (SELECT MAX(insert_dt) FROM cnmcmob.nw_ssa_wise_availability 
				WHERE DATE_FORMAT(insert_dt,'%Y%m') ='$ym' AND param='site_cat')
				GROUP BY circle_id,ssa_id,param_type";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function catWiseSsaAvailability($ym, $circle_id) {
		$sql = "SELECT ssa_id,param_type,SUM(dur) dur, SUM(site_count) site_count, MAX(days) days FROM (
				SELECT circle_id, ssa_id, param_type,SUM(dur) dur, MAX(total_site_count) site_count, MAX(insert_dt) INSERT_dt, 
				DAY(MAX(insert_dt)) days FROM cnmcmob.nw_ssa_wise_availability 
				WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym' AND param='site_cat' and circle_id='$circle_id' AND ssa_id NOT LIKE '%ZZZ'
				GROUP BY ssa_id,param_type) aa 
				GROUP BY ssa_id, param_type";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function __destruct(){
		mysqli_close($this->conn);
	}
}
