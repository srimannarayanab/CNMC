<?php
require_once __DIR__.'/../config/env.inc';

class BtsDown {
	function __construct(){
		$this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysqli_connect_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysqli_connect_error());
	}
	
	function btsDownCategoryWise(){
		$sql = "SELECT coalesce(state_id,'Total') Circle_id, 
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='SUPER_CRITICAL' then 1 ELSE 0 END)  sc,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='CRITICAL' then 1 ELSE 0 END)  cri,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='IMPORTANT' then 1 ELSE 0 END)  imp,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND (site_category='NORMAL' or site_category is null) then 1 ELSE 0 END)  nor,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
				sum(case when site_category='SUPER_CRITICAL' then 1 else 0 end) sc_cnt,
				sum(case when site_category='CRITICAL' then 1 else 0 end) c_cnt,
				sum(case when site_category='IMPORTANT' then 1 else 0 end) imp_cnt,
				sum(case when site_category='NORMAL' or site_category is null then 1 else 0 end) nor_cnt
				FROM ntmsdb.m_bts_master where e_p !='P'
				GROUP BY state_id WITH rollup";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsDownCategoryWiseSSA($circle_id){
		$sql = "SELECT coalesce(ssa_id,'Total') ssaid, group_concat(distinct circle_id) circle_id,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='SUPER_CRITICAL' then 1 ELSE 0 END)  sc,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='CRITICAL' then 1 ELSE 0 END)  cri,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND site_category='IMPORTANT' then 1 ELSE 0 END)  imp,
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND (site_category='NORMAL' or site_category is null) then 1 ELSE 0 END)  nor,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
				sum(case when site_category='SUPER_CRITICAL' then 1 else 0 end) sc_cnt,
				sum(case when site_category='CRITICAL' then 1 else 0 end) c_cnt 
				FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p !='P' 
				GROUP BY ssa_id WITH rollup";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsDownSiteTypeWIse(){
//		$sql = "SELECT coalesce(circle_id,'Total') circle,
//			SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
//			SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
//			SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
//			SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
//			SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
//			SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT
//			FROM ntmsdb.m_bts_master where e_p !='P' GROUP BY circle_id WITH rollup";		
		$sql = "SELECT aa.* FROM (
				SELECT  circle_id circle,
				SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
				SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
				SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
				SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
				SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
				SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT,
				ROUND(SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where e_p ='E' GROUP BY circle_id ORDER BY 8) aa
				UNION ALL 
				SELECT  'Total' circle,
				SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
				SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
				SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
				SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
				SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
				SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT,
				ROUND(SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where e_p ='E'";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsDownSiteTypeSSAWIse($circle_id){
//		$sql = "SELECT coalesce(ssa_id,'Total') ssaid,
//			SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
//			SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
//			SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
//			SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
//			SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
//			SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT
//			FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p !='P' GROUP BY ssa_id WITH rollup";
		$sql = "SELECT aa.* FROM (SELECT ssa_id ssaid,
				SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
				SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
				SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
				SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
				SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
				SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT,
				ROUND(SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p ='E' GROUP BY ssa_id ORDER BY 8) aa
				UNION ALL
				SELECT 'Total' ssaid,
				SUM(case when site_type='BS' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
				SUM(case when site_type='NB' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
				SUM(case when site_type='IP' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) IP,
				SUM(case when site_type='US' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
				SUM(case when site_type='UI' AND bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
				SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END) TOT,
				ROUND(SUM( case when bts_status=0 AND bts_cell_Cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p ='E'";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsDownDurationWise(){
		$sql = "SELECT aa.* FROM (SELECT coalesce(circle_id,'Total') circle,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND days<1 then 1 ELSE 0 END) lt_24,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 1 AND 2 then 1 ELSE 0 END) d_1,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 2 AND 3 then 1 ELSE 0 END) d_2,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 3 AND 7 then 1 ELSE 0 END) d_3,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days > 7 then 1 ELSE 0 END) d_7,			
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
				round(SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc FROM (
				SELECT a.circle_id,a.ssa_id,a.site_type,a.bts_status,a.bts_cell_cnt, a.bts_down_cell_cnt,
				TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days FROM ntmsdb.m_bts_master a where e_p ='E') b
				GROUP BY circle_id ORDER BY 8) aa
				UNION ALL
				SELECT 'Total' circle,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND days<1 then 1 ELSE 0 END) lt_24,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 1 AND 2 then 1 ELSE 0 END) d_1,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 2 AND 3 then 1 ELSE 0 END) d_2,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 3 AND 7 then 1 ELSE 0 END) d_3,
				SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days > 7 then 1 ELSE 0 END) d_7,			
				SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
				round(SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) cnt FROM (
				SELECT a.circle_id,a.ssa_id,a.site_type,a.bts_status,a.bts_cell_cnt, a.bts_down_cell_cnt,
				TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days FROM ntmsdb.m_bts_master a where e_p ='E') b";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsDownDurationSSAWIse($circle_id){
		$sql = "SELECT aa.* FROM (
					SELECT COALESCE(ssa_id,'Total') ssaid,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND days<1 then 1 ELSE 0 END) lt_24,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 1 AND 2 then 1 ELSE 0 END) d_1,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 2 AND 3 then 1 ELSE 0 END) d_2,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 3 AND 7 then 1 ELSE 0 END) d_3,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days > 7 then 1 ELSE 0 END) d_7,
					SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
					ROUND(SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc FROM (
					SELECT a.circle_id,a.ssa_id,a.site_type,a.bts_status,a.bts_cell_cnt, a.bts_down_cell_cnt,
					TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days FROM ntmsdb.m_bts_master a WHERE circle_id='$circle_id' and e_p ='E') b
					GROUP BY ssa_id ORDER BY 8) aa 
					UNION ALL 
					SELECT 'Total' ssaid,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt AND days<1 then 1 ELSE 0 END) lt_24,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 1 AND 2 then 1 ELSE 0 END) d_1,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 2 AND 3 then 1 ELSE 0 END) d_2,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days BETWEEN 3 AND 7 then 1 ELSE 0 END) d_3,
					SUM(case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and days > 7 then 1 ELSE 0 END) d_7,
					SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) Total,
					ROUND(SUM(Case when bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc FROM (
					SELECT a.circle_id,a.ssa_id,a.site_type,a.bts_status,a.bts_cell_cnt, a.bts_down_cell_cnt,
					TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days FROM ntmsdb.m_bts_master a 
					WHERE circle_id='$circle_id' and e_p ='E') b";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsPartialDown(){				
		$sql = "SELECT aa.* FROM (SELECT coalesce(circle_id,'Total') circle,
				SUM(case when bts_type='G' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) pdown_g,
				SUM(case when bts_type='U' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_u,
				SUM(case when bts_type='L' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_l,
				SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) Total,
				round(SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END )/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where e_p ='E'
				GROUP BY circle_id ORDER BY 6) aa
				UNION all
				SELECT 'Total' circle,
				SUM(case when bts_type='G' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) pdown_g,
				SUM(case when bts_type='U' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_u,
				SUM(case when bts_type='L' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_l,
				SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) Total,
				round(SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END )/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master where e_p ='E'";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsPartialDownSSAWise($circle_id){
		$sql = "SELECT aa.* FROM (
				SELECT ssa_id ssaid,
				SUM(case when bts_type='G' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) pdown_g,
				SUM(case when bts_type='U' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_u,
				SUM(case when bts_type='L' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_l,
				SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) Total,
				ROUND(SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END )*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master WHERE circle_id='$circle_id' and e_p ='E' 
				GROUP BY ssa_id ORDER BY 6) aa 
				UNION all
				SELECT 'Total' ssaid,
				SUM(case when bts_type='G' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) pdown_g,
				SUM(case when bts_type='U' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_u,
				SUM(case when bts_type='L' and bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 end) pdown_l,
				SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END ) Total,
				ROUND(SUM(case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END )*100/COUNT(*),2) perc 
				FROM ntmsdb.m_bts_master WHERE circle_id='$circle_id' and e_p ='E'";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function downDetailsDurationWise($circle_id, $ssa_id, $criteria) {
		if($criteria == '1' ) {
			$c=" and days <1";
		} elseif($criteria =='2'){
			$c=" and days between 1 and 2";
		} elseif($criteria =='3'){
			$c =" and days between 2 and 3";
		} elseif($criteria =='4'){
			$c =" and days between 3 and 7";
		} elseif($criteria == '5'){
			$c =" and days >7";
		} else{
			$c ='';
		}

		$sql="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
			e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
			replace(replace(f.fault_type,char(13),''),char(10),'') fault_type, g.outsrc_name 
			FROM (SELECT * FROM (SELECT a.*,TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days
							FROM ntmsdb.m_bts_master a WHERE bts_status=0 and bts_cell_cnt=bts_down_cell_cnt) b where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
			AND bts_cell_cnt=bts_down_cell_cnt and e_p <>'P'".$c.") b
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
			LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
		// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function downDetailsSiteTypeWise($circle_id, $ssa_id, $criteria){
		$sql="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
			e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
			replace(replace(f.fault_type,char(13),''),char(10),'') fault_type, g.outsrc_name 
			FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
			AND site_type LIKE '$criteria' and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
			LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
		
	}
	
	function downDetailsLeasedout($circle_id, $ssa_id, $criteria){	
		$sql="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
			e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
			replace(replace(f.fault_type,char(13),''),char(10),'') fault_type, g.outsrc_name 
			FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
			AND site_type LIKE '$criteria' and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P' and operator_id<>'22' and site_type<>'IP') b
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
			LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
		
	}
	
	function downDetailsCategoryWise($circle_id, $ssa_id, $criteria){
		if($criteria == "NORMAL") {
		  $sql = "SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,replace(replace(f.fault_type,char(13),''),char(10),'') fault_type,
g.outsrc_name
FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
AND (site_category LIKE '$criteria'  or site_category is null) and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b
LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
		} else {
		 $sql="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,replace(replace(f.fault_type,char(13),''),char(10),'') fault_type,
g.outsrc_name 
FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
AND site_category LIKE '$criteria' and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b
LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
		}
		//print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
		
	}
	
	function downDetailsPartial($circle_id, $ssa_id, $criteria){
		/*$sql = "SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,b.site_category,b.operator_id,c.ssa_name,d.vendor_name, TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()) cumm_down_time,
		case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type, 
		case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
		e.fault_id,e.fault_updated_by, e.fault_update_date,f.fault_type 
		FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id' 
		and bts_type like '$criteria' and bts_cell_cnt<>bts_down_cell_cnt and e_p <>'P' ) b 
		LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id LEFT JOIN cnmcmob.app_fault_reasons e ON b.bts_id=e.bts_id
		AND b.bts_status_dt=e.bts_status_dt LEFT JOIN ntmsdb.fault_master f ON e.fault_id=f.fault_id 
		ORDER BY bts_status_dt asc";*/

		$sql ="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,replace(replace(f.fault_type,char(13),''),char(10),'') fault_type
FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id'
                and bts_type like '$criteria' and bts_cell_cnt<>bts_down_cell_cnt and e_p <>'P') b
LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id ORDER BY b.bts_status_dt asc";
//		print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function circleWiseCurrentAvailability(){
		$sql = "SELECT circle_id circle, down,partial_down, total, 100-ROUND((down+partial_down)*100/total,2) perc_availability From (
				SELECT circle_id, SUM(case When bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) down,
				SUM(Case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END) partial_down,
				COUNT(*) total  FROM ntmsdb.m_bts_master where e_p <>'P' 
				GROUP BY circle_id) a ORDER BY 5 DESC";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function ssaWiseCurrentAvailability($circle_id){
		$sql = "SELECT ssa_id circle, down,partial_down, total, 100-ROUND((down+partial_down)*100/total,2) perc_availability From (
				SELECT ssa_id, SUM(case When bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) down,
				SUM(Case when bts_status=0 AND bts_cell_cnt<>bts_down_cell_cnt then 1 ELSE 0 END) partial_down,
				COUNT(*) total  FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p <>'P'
				GROUP BY ssa_id) a ORDER BY 5 DESC";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsdownLeasedOut(){			
		$sql ="SELECT aa.* FROM (
					SELECT circle_id circle,
					SUM(Case when site_type='BS' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
					SUM(Case when site_type='NB' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
					SUM(Case when site_type='US' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
					SUM(Case when site_type='UI' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
					SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END) TOT,
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END) CNT,
					ifnull(ROUND(SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END)*100/
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END),2),0) perc
					FROM ntmsdb.m_bts_master where e_p ='E'
					GROUP BY circle_id ORDER BY 8,7) aa 
					UNION all
					SELECT 'Total' circle,
					SUM(Case when site_type='BS' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
					SUM(Case when site_type='NB' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
					SUM(Case when site_type='US' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
					SUM(Case when site_type='UI' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
					SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END) TOT,
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END) CNT,
					ROUND(SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END)*100/
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END),2) perc
					FROM ntmsdb.m_bts_master where e_p ='E'";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function btsdownLeasedOutSSA($circle_id){
		$sql = "SELECT aa.* FROM (
					SELECT ssa_id ssaid,
					SUM(Case when site_type='BS' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
					SUM(Case when site_type='NB' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
					SUM(Case when site_type='US' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
					SUM(Case when site_type='UI' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
					SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END) TOT,
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END) CNT,
					ifnull(ROUND(SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END)*100/
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END),2),0) perc
					FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p ='E'
					GROUP BY ssa_id ORDER BY 8) aa
					UNION all
					SELECT 'Total' ssaid,
					SUM(Case when site_type='BS' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) BS,
					SUM(Case when site_type='NB' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) NB,
					SUM(Case when site_type='US' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) US,
					SUM(Case when site_type='UI' and bts_status=0 and site_type<>'IP' AND operator_id<>'22' and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) UI,
					SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END) TOT,
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END) CNT,
					ROUND(SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt and site_type<>'IP' AND operator_id<>'22' then 1 ELSE 0 END)*100/
					SUM(case when site_type<>'IP' AND operator_id<>'22'then 1 ELSE 0 END),2) perc
					FROM ntmsdb.m_bts_master where circle_id='$circle_id' and e_p ='E'";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function circleMttr($ym){
		$sql = "SELECT s.*, @rownum :=@rownum+1 AS num FROM (
				SELECT aa.* FROM (
				SELECT coalesce(circle_id,'TOT') circleid, SUM(total_bts_count) site_count, round(SUM(dur)/60) dur, SUM(dist_bts) distinct_btsid,
				SUM(INSTANCE) instance, round(sum(dur)/sum(INSTANCE)) mttr_m, concat(floor(round(SUM(dur)/SUM(INSTANCE))/60), 'h: ',MOD(round(SUM(dur)/SUM(INSTANCE)),60),'m') mttr
				FROM ntmsdb.mttr_ssa_month_report
				WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym' group BY circle_id ORDER BY mttr_m) aa
				UNION ALL				
				SELECT 'TOT' total, SUM(total_bts_count) site_count, round(SUM(dur)/60) dur, SUM(dist_bts) distinct_btsid,
				SUM(INSTANCE) instance, round(sum(dur)/sum(INSTANCE)) mttr_m, concat(floor(round(SUM(dur)/SUM(INSTANCE))/60), 'h: ',MOD(round(SUM(dur)/SUM(INSTANCE)),60),'m') mttr
				FROM ntmsdb.mttr_ssa_month_report
				WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym') s , (SELECT @rownum := 0) r";
//		 print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function ssaMttr($ym, $circle_id) {
		$sql = "SELECT s.*, @rownum :=@rownum+1 AS num FROM (
				select aa.* FROM (SELECT  ssa_id ssaid, SUM(total_bts_count) site_count, round(SUM(dur)/60) dur, SUM(dist_bts) distinct_btsid,
				SUM(INSTANCE) instance, round(SUM(dur)/SUM(INSTANCE)) mttr_m ,concat(floor(round(SUM(dur)/SUM(INSTANCE))/60), 'h: ',MOD(round(SUM(dur)/SUM(INSTANCE)),60),'m') mttr
				FROM ntmsdb.mttr_ssa_month_report
				WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym' AND circle_id='$circle_id' GROUP BY ssa_id ORDER BY mttr_m) aa
				UNION all
				SELECT  'TOT' ssaid, SUM(total_bts_count) site_count, round(SUM(dur)/60) dur, SUM(dist_bts) distinct_btsid,
				SUM(INSTANCE) INSTANCE, round(SUM(dur)/SUM(INSTANCE)) mttr_m ,concat(floor(round(SUM(dur)/SUM(INSTANCE))/60), 'h: ',MOD(round(SUM(dur)/SUM(INSTANCE)),60),'m') mttr
				FROM ntmsdb.mttr_ssa_month_report
				WHERE DATE_FORMAT(insert_dt,'%Y%m')='$ym' AND circle_id='$circle_id') s, (SELECT @rownum := 0) r";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function getBtsInfo($bts_id){
            /*$sql = "SELECT b.bts_id,b.bts_name,b.bts_latitude, b.bts_longitude,b.bts_location,b.bts_status_dt,b.site_category,b.operator_id,c.ssa_name,f.CIRCLE_NAME,vendor_name , TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()) cumm_down_time,
                case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
                case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype,
                e.fault_id, e.fault_updated_by, e.fault_update_date 
                FROM (SELECT a.*,TIMESTAMPDIFF(SECOND,bts_status_dt, NOW())/86400 AS days 
                FROM ntmsdb.m_bts_master a WHERE a.bts_id='$bts_id') b LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID
                LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id LEFT JOIN cnmcmob.app_fault_reasons e ON
                b.bts_id=b.bts_id LEFT JOIN ntmsdb.circle_master f ON c.circle_id=f.CIRCLE_ID";*/
	$sql ="SELECT b.bts_id,b.bts_name,b.bts_latitude, b.bts_longitude,b.bts_location,b.bts_status_dt,b.site_category,b.operator_id,c.ssa_name,vendor_name , TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()) cumm_down_time, g.CIRCLE_NAME,
case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type, 
case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
if(b.bts_status_dt<e.added_dt, e.bts_down_cause, NULL) fault_id,
if(b.bts_status_dt<e.added_dt,e.added_by, NULL) fault_updated_by, 
if(b.bts_status_dt<e.added_dt,e.added_dt, NULL) fault_update_date,
if(b.bts_status_dt<e.added_dt,f.fault_type, NULL) fault_type 
FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND bts_id='$bts_id' and e_p !='P') b 
	LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id LEFT JOIN (SELECT aaa.bts_id,aaa.bts_down_cause,
                        aaa.added_by,aaa.added_dt FROM ntmsdb.m_bts_status_log aaa LEFT JOIN ntmsdb.m_bts_status_log bbb ON aaa.bts_id=bbb.bts_id AND aaa.added_dt<bbb.added_dt WHERE bbb.bts_id IS NULL) e 
                        ON b.bts_id=e.bts_id LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
								LEFT JOIN ntmsdb.circle_master g ON c.circle_id=g.CIRCLE_ID";
	//print($sql);
		$res = mysqli_query($this->conn , $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return $row[0];
	}

	function updateBtsFaultReason($bts_id, $fault_id, $bts_status_dt, $fault_updated_by){
//		$sql = "replace into cnmcmob.app_fault_reasons(bts_id, fault_id, bts_status_dt, fault_updated_by,fault_update_date) values('$bts_id', '$fault_id', '$bts_status_dt', '$fault_updated_by', sysdate())";
                $sql = "insert into ntmsdb.m_bts_status_log(bts_id,bts_down_cause,added_by,added_dt) values('$bts_id','$fault_id','$fault_updated_by',sysdate())";
		$res = mysqli_query($this->conn, $sql);
		// print($sql);
		// print(mysqli_affected_rows($this->conn));
		if(mysqli_affected_rows($this->conn)>0) {
			return array('result'=>'ok', 'error'=>mysqli_error($this->conn));
		} else {
			return array('result'=>'fail', 'error'=>mysqli_error($this->conn));
		}
	}

	function circleLockedSites(){
		$sql = "SELECT ab.*,round(ab.cnt*100/ab.m_cnt,2) perc FROM (
				SELECT aaa.*, bbb.m_cnt FROM (
				SELECT coalesce(aa.circle_id,'TOT') circle_id, SUM(if(aa.make='Ericsson',1,0)) AS eric_cnt,
				SUM(if(aa.make='alcatel',1,0)) AS alc_cnt,
				SUM(if(aa.make='Motorola',1,0)) AS moto_cnt,
				SUM(if(aa.make='Huawei',1,0)) AS hua_cnt,
				SUM(if(aa.make='Nortel',1,0)) AS nor_cnt,
				SUM(if(aa.make='Nokia',1,0)) AS nok_cnt,
				SUM(if(aa.make='ZTE',1,0)) AS zte_cnt, COUNT(*) cnt FROM
				(SELECT a.circle_id,a.bts_site_id,a.bsc_name,a.make FROM ntmstest.m_bts_lock_master a,ntmsdb.m_bts_master b
				WHERE a.bts_site_id=b.bts_site_id AND a.circle_id=b.circle_id and a.bsc_id = b.bsc_id and b.e_p='E'
				GROUP BY a.bts_site_id
				ORDER BY a.circle_id) aa
				group BY aa.circle_id) aaa,(
				SELECT circle_id,COUNT(*) m_cnt FROM ntmsdb.m_bts_master WHERE e_p='E'
				GROUP BY circle_id) bbb
				WHERE aaa.circle_id=bbb.circle_id) ab ORDER BY perc asc";
        $res = mysqli_query($this->conn, $sql);
        while($row[] = mysqli_fetch_assoc($res));
        return array_filter($row);
	}

	function ssaLockedSites($circle){
		$sql = "SELECT ab.*, ROUND(ab.cnt*100/ab.m_cnt,2) perc FROM (
			SELECT aaa.*,bbb.m_cnt FROM (
			SELECT coalesce(aa.ssa_id,'TOT') ssa_id, SUM(if(aa.make='Ericsson',1,0)) AS eric_cnt,
			SUM(if(aa.make='alcatel',1,0)) AS alc_cnt,
			SUM(if(aa.make='Motorola',1,0)) AS moto_cnt,
			SUM(if(aa.make='Huawei',1,0)) AS hua_cnt,
			SUM(if(aa.make='Nortel',1,0)) AS nor_cnt,
			SUM(if(aa.make='Nokia',1,0)) AS nok_cnt,
			SUM(if(aa.make='ZTE',1,0)) AS zte_cnt,
			COUNT(*) cnt FROM
			(SELECT a.circle_id,b.ssa_id,a.bts_site_id,a.bsc_name,a.make FROM ntmstest.m_bts_lock_master a,ntmsdb.m_bts_master b
			WHERE a.bts_site_id=b.bts_site_id AND a.circle_id=b.circle_id AND a.circle_id='$circle' and a.bsc_id = b.bsc_id and b.e_p='E'
			GROUP BY a.bts_site_id
			ORDER BY a.ssa_id) aa
			group BY aa.ssa_id )aaa, (SELECT ssa_id,COUNT(*) m_cnt FROM ntmsdb.m_bts_master WHERE circle_id='$circle' AND e_p='E' 
			GROUP BY ssa_id) bbb
			WHERE aaa.ssa_id=bbb.ssa_id) ab ORDER BY perc desc";
        $res = mysqli_query($this->conn, $sql);
        while($row[] = mysqli_fetch_assoc($res));
        return array_filter($row);
	}

	function lockedSitesDetails($circle_id, $ssa_id, $vendor_id){
		$sql = "SELECT m.* FROM (SELECT main.*,sub.fault_type FROM (SELECT a.circle_id,b.ssa_id,a.bts_name,a.bts_site_id,a.bsc_name,s.ssa_name,c.city_name,a.make,a.insert_dt 
                    FROM ntmstest.m_bts_lock_master a,ntmsdb.m_bts_master b,ntmsdb.ssa_master s,ntmsdb.city_master c
                    WHERE
                      a.bts_site_id=b.bts_site_id
                    AND a.circle_id =b.circle_id
                    AND  b.ssa_id=s.SSA_ID
					AND b.bsc_id=a.bsc_id
                    AND  b.city_id=c.city_id
                    AND a.circle_id like '$circle_id' and b.ssa_id like '$ssa_id' AND a.vendor_id LIKE '$vendor_id') main
                    LEFT join
                    (select distinct a.bts_site_id,a.added_dt,a.bts_down_cause,b.fault_type from ntmstest.m_bts_status_log_lock a,ntmsdb.fault_master b
                    where log_id IN (select max(log_id) from ntmstest.m_bts_status_log_lock group by bts_site_id) and a.bts_down_cause=b.fault_id) sub
                    ON main.bts_site_id = sub.bts_site_id) m
                    GROUP BY m.bts_site_id
                    ORDER BY m.circle_id";
        // print($sql);
        $res = mysqli_query($this->conn, $sql);
        while($row[] = mysqli_fetch_assoc($res));
        return array_filter($row);
	}
        
        function techWise(){
            $sql = "SELECT coalesce(circle_id,'Total') circle_id,SUM(case when bts_type='G'  then 1 ELSE 0 END) bts_2g_cnt,
                SUM(case when bts_type='U' then 1 ELSE 0 END) bts_3g_cnt,
                SUM(case when bts_type='L' then 1 ELSE 0 END) bts_4g_cnt,
                SUM(case when bts_type='G' AND bts_cell_cnt = bts_down_cell_cnt and bts_status=0  then 1 ELSE 0 END) bts_2g_down_cnt,
                SUM(case when bts_type='U' and bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END) bts_3g_down_cnt,
                SUM(case when bts_type='L' and bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END) bts_4g_down_cnt,
                SUM(case when bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END) down_cnt,COUNT(*) total_cnt,
                ROUND(SUM(case when bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END)*100/COUNT(*),2) perc
                FROM ntmsdb.m_bts_master WHERE e_p='E'
                GROUP BY circle_id WITH rollup";
//            print($sql);
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            $data = array_filter($row);
	    $total = array_pop($data);
            array_multisort( array_column($data, "perc"), SORT_ASC, $data );
            array_push($data, $total);

	  return $data;
        }
        
        function techWiseCircle($circle){
	    $sql ="SELECT coalesce(ssa_id,'Total') ssa_id, GROUP_CONCAT(distinct circle_id) circle_id,SUM(case when bts_type='G' then 1 ELSE 0 END) bts_2g_cnt,
                SUM(case when bts_type='U' then 1 ELSE 0 END) bts_3g_cnt,
                SUM(case when bts_type='L' then 1 ELSE 0 END) bts_4g_cnt,
                SUM(case when bts_type='G' AND bts_cell_cnt = bts_down_cell_cnt and bts_status=0  then 1 ELSE 0 END) bts_2g_down_cnt,
                SUM(case when bts_type='U' AND bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END) bts_3g_down_cnt,
                SUM(case when bts_type='L' AND bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END) bts_4g_down_cnt,
                SUM(case when bts_status=0 AND bts_cell_cnt = bts_down_cell_cnt then 1 ELSE 0 END) down_cnt,COUNT(*) total_cnt,
                ROUND(SUM(case when bts_cell_cnt = bts_down_cell_cnt and bts_status=0 then 1 ELSE 0 END)*100/COUNT(*),2) perc
                FROM ntmsdb.m_bts_master WHERE e_p='E' AND circle_id='$circle'
                GROUP BY ssa_id WITH rollup";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            $data = array_filter($row);        
            $total = array_pop($data);
            array_multisort( array_column($data, "perc"), SORT_ASC, $data );
            array_push($data, $total);

          return $data;$total = array_pop($data);
            array_multisort( array_column($data, "perc"), SORT_ASC, $data );
            array_push($data, $total);

          return $data;
        }
        function techWisedownDetails($circle_id, $ssa_id, $bts_type){
	   $sql ="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
				case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type, 
				case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
				e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
				replace(replace(f.fault_type,char(13),''),char(10),'') fault_type , g.outsrc_name 
				FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id like '$circle_id' and ssa_id LIKE '$ssa_id' 
				AND a.bts_type like '$bts_type' and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b 
				LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id 
				LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
				LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
				LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
	   #print($sql);
	   $res = mysqli_query($this->conn, $sql);
	   while($row[] = mysqli_fetch_assoc($res));
	   return array_filter($row);
	}

	function getBtsDownCause($bts_id){
	  $sql ="SELECT b.bts_id,b.bts_name,b.bts_latitude, b.bts_longitude,b.bts_location,b.bts_status_dt,b.site_category,b.operator_id,c.ssa_name,vendor_name , 
			TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()) cumm_down_time, g.CIRCLE_NAME,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type, 
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype,
			b.bts_id log_id, e.added_by, e.added_dt,f.fault_type, h.outsrc_name, b.bts_down_cell_cnt,b.bts_status  
			FROM ntmsdb.m_bts_master b 
        	LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID 
			LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id 
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND e.bts_down_dt>=b.bts_status_dt 
		   LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.circle_master g ON c.circle_id=g.CIRCLE_ID 
			LEFT JOIN ntmsdb.outsource_master h ON b.outsrc_id=h.outsrc_id 
			WHERE b.bts_id='$bts_id' AND b.e_p !='P' 
			AND (b.bts_status_dt=e.bts_down_dt OR e.bts_down_dt IS NULL)";

	   $res = mysqli_query($this->conn, $sql);
	   while($row[] = mysqli_fetch_assoc($res));
	   return $row[0];
	}

	function updateBtsDownFaultReason($bts_id, $bts_down_cell_cnt, $bts_down_dt, $bts_status, $bts_down_cause, $added_by){
		$sql = "insert into ntmsdb.m_bts_fault_report_down_bts(bts_id, bts_down_cell_cnt, bts_down_dt, bts_status, bts_down_cause,added_by, added_dt)
		values('$bts_id','$bts_down_cell_cnt','$bts_down_dt','$bts_status','$bts_down_cause','$added_by',sysdate()) 
		on duplicate key update bts_down_cause=values(bts_down_cause), added_by=values(added_by), added_dt=sysdate()";
	  #print($sql);
	  $res = mysqli_query($this->conn, $sql);
                if(mysqli_affected_rows($this->conn)>0) {
                        return array('result'=>'ok', 'error'=>mysqli_error($this->conn));
                } else {
                        return array('result'=>'fail', 'error'=>mysqli_error($this->conn));
                }
	}

	function btsdownOutsourcedWise(){
		$sql ="SELECT coalesce(circle_id,'Total') circle, 
			SUM(case when outsrc_id<>10 AND bts_status=0 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) outsource_down,
			SUM(case when outsrc_id<>10 then 1 ELSE 0 END) outsourced_sites,
			SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) bsnl_down,
			COUNT(*) bsnl_sites,
			round(SUM(case when outsrc_id<>10 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc
			FROM ntmsdb.m_bts_master where e_p !='P' AND site_type<>'IP'
			GROUP BY circle_id WITH ROLLUP";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function btsdownOutsourcedSSAWise($circle_id){
		$sql ="SELECT coalesce(ssa_id,'Total') ssaname,group_concat(distinct circle_id) circle_id,
                        SUM(case when outsrc_id<>10 AND bts_status=0 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) outsource_down,
                        SUM(case when outsrc_id<>10 then 1 ELSE 0 END) outsourced_sites,
                        SUM(case when bts_status=0 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END) bsnl_down,
                        COUNT(*) bsnl_sites,
                        round(SUM(case when outsrc_id<>10 and bts_cell_cnt=bts_down_cell_cnt then 1 ELSE 0 END)*100/COUNT(*),2) perc
                        FROM ntmsdb.m_bts_master where e_p !='P' AND site_type<>'IP' AND circle_id='$circle_id'
                        GROUP BY ssa_id WITH ROLLUP";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function downDetailsOutsourced($circle_id, $ssa_id){
		$sql = "SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type, 
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
			e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
			replace(replace(f.fault_type,char(13),''),char(10),'') fault_type , g.outsrc_name 
			FROM (SELECT * FROM ntmsdb.m_bts_master a where bts_status=0 AND circle_id= '$circle_id' and ssa_id LIKE '$ssa_id' 
			AND outsrc_id<>10 and bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b 
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id 
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt 
			AND b.bts_cell_cnt=e.bts_down_cell_cnt
			LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
		$res =mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function getOMCRProcess($circle_id){
		$sql ="SELECT a.*,b.omcr_name FROM (
				SELECT * FROM ntmsdb.m_omcr_process_log WHERE (circle_id,omcr_id,start_dt) IN (
				SELECT circle_id,omcr_id,MAX(start_dt) start_dt FROM 
				ntmsdb.m_omcr_process_log WHERE circle_id='$circle_id' AND process_status='S'
				GROUP BY circle_id, omcr_id)) a INNER JOIN ntmsdb.m_omcr_master b 
				ON a.omcr_id=b.omcr_id";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}
	
	function __destruct(){
		mysqli_close($this->conn);
	}
}
