<?php
require_once __DIR__.'/../config/env.inc';

class traffic {
	function __construct(){
		$this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysql_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysql_error());
	}
	

	function get2GTraffic(){
		$sql = "SELECT circle_id, ssa_id, round(sum(cell_traffic),2) cell_traffic, ROUND((sum(data_vol_dn)+sum(data_vol_up))/(1024*1024*1024),2) data_vol 
				FROM (
				SELECT a.circle_id,a.ssa_id , cell_traffic,replace(data_vo_dn,',','')/8  data_vol_dn,replace(data_vo_up,',','')/8 data_vol_up from ntmstest.qos_data_zte_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=1 
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic, replace(data_vo_dn,',',''),replace(data_vo_up,',','') from ntmstest.qos_data_eric_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=8
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic, 0 data_ov_dn,0 data_vol_up FROM ntmstest.qos_data_eric_2gDay_individual a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=8
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic, replace(data_vo_dn,',','') data_ov_dn,replace(data_vo_up,',','') data_vol_up from ntmstest.qos_data_huawei_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=6 
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic,replace(data_vo_dn,',','') data_ov_dn,replace(data_vo_up,',','') data_vol_up from ntmstest.qos_data_nokia_sw_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=3
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic,replace(data_vo_dn,',','') data_ov_dn,replace(data_vo_up,',','') data_vol_up from ntmstest.qos_data_nortel_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=5
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic,replace(data_vo_dn,',','') data_ov_dn,replace(data_vo_up,',','') data_vol_up from ntmstest.qos_data_motorola_2g_day a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=4
				UNION 
				SELECT a.circle_id,a.ssa_id , cell_traffic cell_traffic,replace(data_vo_dn,',','') data_ov_dn,replace(data_vo_up,',','') data_vol_up from ntmstest.qos_data_alcatel_2g_day_voice a , ntmsdb.ssa_master b
				WHERE a.ssa_id = b.SSA_ID AND  a.circle_id = b.CIRCLE_ID and a.vendor_id=2
				) trf_tbl GROUP BY circle_id, ssa_id";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function get3GTraffic(){
		$sql = "SELECT circle_id, ssa_id, round(SUM(cell_traffic),2) cell_traffic, ROUND((SUM(data_vol_dn)+SUM(data_vol_up))/(1024*1024),2)  data_vol FROM (
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','') data_vol_dn, REPLACE(a.data_vo_up,',','') data_vol_up 
				FROM ntmstest.qos_data_zte_3g a, ntmsdb.ssa_master b 
				where a.ssa_id = b.SSA_ID AND a.circle_id = b.CIRCLE_ID  and a.vendor_id=1
				UNION 
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','') data_vol_dn, REPLACE(a.data_vo_up,',','') data_vol_up 
				FROM ntmstest.qos_data_alcatel_3g a, ntmsdb.ssa_master b 
				where a.ssa_id = b.SSA_ID and a.circle_id = b.CIRCLE_ID  and a.vendor_id=2
				UNION 
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','')*1024 data_vol_dn, REPLACE(a.data_vo_up,',','')*1024 data_vol_up 
				FROM ntmstest.qos_data_nokia_sw_3g a, ntmsdb.ssa_master b  
				where a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID  and a.vendor_id=3
				UNION 
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','') data_vol_dn, REPLACE(a.data_vo_up,',','') data_vol_up 
				FROM ntmstest.qos_data_huawei_3g a, ntmsdb.ssa_master b 
				where a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID  and a.vendor_id=6
				UNION 
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','') data_vol_dn, REPLACE(a.data_vo_up,',','') data_vol_up 
				FROM ntmstest.qos_data_eric_3g a, ntmsdb.ssa_master b 
				where a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID  and a.vendor_id=8
				UNION 
				SELECT a.circle_id,a.ssa_id,a.cell_traffic, REPLACE(a.data_vo_dn,',','') data_vol_dn, REPLACE(a.data_vo_up,',','') data_vol_up 
				FROM ntmstest.qos_data_eric_3g_individual a, ntmsdb.ssa_master b 
				where a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID  and a.vendor_id=8) trf_tbl
				GROUP BY circle_id, ssa_id";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function get4GTraffic(){
		$sql = "SELECT circle_id, ssa_id, round(SUM(cell_traffic),2) cell_traffic, round(SUM(data_vol)/1024,2) data_vol FROM (
				SELECT a.circle_id, a.ssa_id, cell_traffic, a.data_vo_dn + a.data_vo_up  data_vol
				FROM ntmstest.qos_data_nokia_W_4G a, ntmsdb.ssa_master b 
				WHERE a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID and a.vendor_id=3
				UNION 
				SELECT a.circle_id, a.ssa_id, cell_traffic, a.data_vo_dn + a.data_vo_up  data_vol
				FROM ntmstest.qos_data_ZTE_E_4G a, ntmsdb.ssa_master b 
				WHERE a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID and a.vendor_id=1 
				UNION 
				SELECT a.circle_id, a.ssa_id, cell_traffic, a.data_vo_dn + a.data_vo_up  data_vol
				FROM ntmstest.qos_data_ZTE_N_4G a, ntmsdb.ssa_master b 
				WHERE a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID and a.vendor_id=1 
				UNION
				SELECT a.circle_id, a.ssa_id, cell_traffic, a.data_vo_dn + a.data_vo_up  data_vol
				FROM ntmstest.qos_data_nokia_S_4G a, ntmsdb.ssa_master b 
				WHERE a.ssa_id = b.SSA_ID  and a.circle_id = b.CIRCLE_ID and a.vendor_id=3 ) aa 
				GROUP BY circle_id, ssa_id";
				
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function circleTraffic($kpi_date){
		$sql = "SELECT coalesce(circle_id,'TOT') circle, ifnull(round(sum(voice_traffic),2),0) erl, ifnull(round(SUM(data_traffic),2),0) data_vol, date(ins_dt) ins_dt,
			sum(bts_file_cnt) traffic_cnt, sum(bts_master_cnt) master_cnt 
			FROM ntmsdb.traffic_summary_ssa_wise WHERE DATE(date_sub(ins_dt,interval 1 day))='$kpi_date'
			GROUP BY circle_id WITH rollup";
		// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		// print_r($row);
		return array_filter($row);
	}

	function ssaTraffic($circle_id, $kpi_date){
		$sql = "SELECT coalesce(ssa_id,'TOT') ssaid, ifnull(round(sum(voice_traffic),2),0) erl, ifnull(round(SUM(data_traffic),2),0) data_vol, date(ins_dt) ins_dt,
			sum(bts_file_cnt) traffic_cnt, sum(bts_master_cnt) master_cnt 
			FROM ntmsdb.traffic_summary_ssa_wise WHERE DATE(date_sub(ins_dt,interval 1 day))='$kpi_date' and circle_id='$circle_id' 
			GROUP BY ssa_id WITH rollup";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	
	function __destruct(){
		mysqli_close($this->conn);
	}
	
	
}
