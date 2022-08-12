<?php
require_once __DIR__.'/../config/env.inc';

class myBts {
        function __construct(){
                $this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysqli_connect_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysqli_connect_error());
        }

	function viewBts($msisdn){
		$sql = "SELECT b.bts_name, b.ssa_id, case when b.bts_type='G' then 'GSM'
		when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
		b.bts_id FROM mybts a INNER JOIN ntmsdb.m_bts_master b ON a.bts_id=b.bts_id 
		WHERE msisdn='$msisdn' AND STATUS=0 order by bts_name";
		// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);

	}

	function addBts($id, $msisdn){
		$sql ="insert into cnmcmob.mybts(msisdn, bts_id,status,add_date) values('$msisdn','$id',0,sysdate()) 
		on duplicate key update status=0, del_date=null, add_date=sysdate()";
		// print($sql);
		$res= mysqli_query($this->conn, $sql);
		if($res){
			return array('result'=>'true', 'error'=>null);
		} else {
			return array('result'=>'false', 'error'=>mysqli_error($this->conn));
		}
	}

	function getUserMyBtsCount($user_privs, $circle_id){
		if($user_privs=='co'){
			$sql = "SELECT coalesce(circle_id,'TOT') c_name, COUNT(msisdn) total_users , COUNT(mybts_msisdn) mybts_cnt,
				sum(case when last_login>DATE(SYSDATE()) then 1 ELSE 0 END) login_count FROM( 
				SELECT aa.circle_id,aa.msisdn,ab.msisdn mybts_msisdn,aa.last_login  FROM cnmcmob.app_users aa LEFT JOIN 
				(SELECT distinct a.circle_id, b.msisdn FROM ntmsdb.m_bts_master a INNER JOIN cnmcmob.mybts b
				ON  a.bts_id=b.bts_id AND b.status=0) ab ON aa.msisdn=ab.msisdn) aaa
				GROUP BY circle_id with rollup";
		} else {
			$sql = "SELECT coalesce(ssa_id,'TOT') c_name, COUNT(msisdn) total_users , COUNT(mybts_msisdn) mybts_cnt,
			sum(case when last_login>DATE(SYSDATE()) then 1 ELSE 0 END) login_count FROM( 
			SELECT aa.circle_id, ifnull(aa.ssa_id,'ZNot-Updt') ssa_id, aa.msisdn,ab.msisdn mybts_msisdn,aa.last_login  FROM cnmcmob.app_users aa LEFT JOIN 
			(SELECT distinct a.circle_id, b.msisdn FROM ntmsdb.m_bts_master a INNER JOIN cnmcmob.mybts b
			ON  a.bts_id=b.bts_id AND b.status=0 ) ab ON aa.msisdn=ab.msisdn where aa.circle_id='$circle_id') aaa
			GROUP BY ssa_id WITH ROLLUP ";
		}
		// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[]=mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function getCircleUsersList($circle_id){
		$sql = "SELECT msisdn, CONVERT(name USING utf8) name, CONVERT(desg USING utf8) desg, CONVERT(email USING utf8) email, 
		hrms_no, ssaname, ssa_id, circle, circle_id,last_login, app_version,lvl, lvl2,lvl3
		from cnmcmob.app_users WHERE  circle_id like '$circle_id' AND STATUS='Y' 
		AND desg NOT IN ('CGM','GM','PGM') AND length(msisdn)>=10 ORDER BY 1 limit 4500 ";
		// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function getCircleMyBtsUsersList($circle_id, $ssa_id){
		if($ssa_id=='TOT'){
			$ssa_id='%';
		}
		$sql = "SELECT msisdn, name, circle, desg, email, COUNT(msisdn) cnt FROM (
			SELECT a.msisdn,a.name, a.circle, a.email, a.desg FROM cnmcmob.app_users a
			INNER JOIN cnmcmob.mybts b 
			ON a.msisdn=b.msisdn  AND a.status='Y' AND b.status=0 AND a.circle_id='$circle_id' and a.ssa_id like '$ssa_id') aa
			GROUP BY msisdn, name, circle, desg,email";
			// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function getMyBtsConfiguredList($mybts_msisdn){
		$sql = "SELECT a.mybts_id,b.bts_name, b.ssa_id, case when b.bts_type='G' then 'GSM'
		when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
		b.bts_id FROM mybts a INNER JOIN ntmsdb.m_bts_master b ON a.bts_id=b.bts_id 
		WHERE msisdn='$mybts_msisdn' AND STATUS=0 order by bts_name";
			// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function unlinkMyBtsID($mybts_id, $msisdn){
		$sql = "update cnmcmob.mybts set status=1, del_date=sysdate(), remarks='$msisdn' where mybts_id='$mybts_id'";
		$res = mysqli_query($this->conn, $sql);
		$res= mysqli_query($this->conn, $sql);
		if($res){
			return array('result'=>'true', 'error'=>null);
		} else {
			return array('result'=>'false', 'error'=>mysqli_error($this->conn));
		}
	}

	function changeMyBtsMsisdn($old_msisdn, $new_msisdn, $msisdn){
		$sql = "update ignore cnmcmob.mybts set msisdn='$new_msisdn',add_date=sysdate(), remarks='$msisdn' where msisdn='$old_msisdn' and status=0";
		$res = mysqli_query($this->conn, $sql);
		if($res){
			return array('result'=>'true', 'error'=>null);
		} else {
			return array('result'=>'false', 'error'=>mysqli_error($this->conn));
		}
	}

	function getTodayLogins($circle_id, $ssa_id){
		if($ssa_id=='TOT'){
			$ssa_id='%';
		}
		$sql = "SELECT name, circle ,desg,email, msisdn, last_login FROM cnmcmob.app_users WHERE circle_id='$circle_id' AND ssa_id LIKE '$ssa_id' AND DATE(last_login)=DATE(SYSDATE()) order by last_login";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}

	function getCircleMyBtsConfiguredList($circle){
		$sql = "SELECT aa.msisdn, aa.name, aa.desg, aa.hrms_no, aa.circle, aa.circle_id, aa.ssaname,
			ab.bts_name, ab.bts_id, ab.ssa_id FROM 
			cnmcmob.app_users aa INNER JOIN (
			SELECT a.bts_name, a.bts_id, a.ssa_id, b.msisdn FROM ntmsdb.m_bts_master a 
			INNER JOIN cnmcmob.mybts b ON a.bts_id=b.bts_id AND b.status=0 AND a.e_p='E') ab
			ON aa.msisdn=ab.msisdn WHERE aa.circle_id='$circle'";
			// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);

	}

	function deleteMyBts($id, $msisdn){
		$sql ="update cnmcmob.mybts set del_date=sysdate(), status=1 where msisdn='$msisdn' and bts_id='$id'";
		$res= mysqli_query($this->conn, $sql);
		if($res){
			return array('result'=>'true', 'error'=>null);
		} else {
			return array('result'=>'false', 'error'=>mysqli_error($this->conn));
		}
	}

	function getBtsSSA($ssa_id){
		$sql ="select bts_name, case when bts_type='G' then 'GSM'
		when bts_type='U' then 'UMTS' when bts_type='L' then 'LTE' end bts_type, bts_id from ntmsdb.m_bts_master where ssa_id='$ssa_id' and e_p<>'P' order by 1";
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);

	}

	function getMyBtsDown($msisdn){
		$sql ="SELECT distinct b.bts_id,b.bts_name,b.bts_status_dt,ifnull(b.site_category,'Normal') site_category,b.operator_id,c.ssa_name,d.vendor_name , ifnull(TIMESTAMPDIFF(SECOND, b.bts_status_dt,SYSDATE()),'') cumm_down_time,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end sitetype ,
			e.bts_down_cause fault_id,e.added_by fault_updated_by, e.added_dt fault_update_date ,
			replace(replace(f.fault_type,char(13),''),char(10),'') fault_type, g.outsrc_name 
			FROM (SELECT aa.* FROM ntmsdb.m_bts_master aa INNER JOIN cnmcmob.mybts ab ON aa.bts_id=ab.bts_id 
			where ab.msisdn='$msisdn' and ab.status=0 and bts_status=0 AND bts_cell_cnt=bts_down_cell_cnt and e_p <>'P') b
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
			LEFT JOIN ntmsdb.m_bts_fault_report_down_bts e ON b.bts_id=e.bts_id AND b.bts_status_dt=e.bts_down_dt AND b.bts_cell_cnt=e.bts_down_cell_cnt
			LEFT JOIN ntmsdb.fault_master f ON e.bts_down_cause=f.fault_id 
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_status_dt asc";
                $res = mysqli_query($this->conn, $sql);
                while($row[] = mysqli_fetch_assoc($res));
                return array_filter($row);

	}

	function getLeftOutMyBts($circle_id){
		$sql ="SELECT distinct b.bts_id,b.bts_name,ifnull(b.site_category,'NORMAL') site_category, e.circle_name, b.operator_id,c.ssa_name,d.vendor_name ,
			case when b.bts_type='G' then 'GSM' when b.bts_type='U' then 'UMTS' when b.bts_type='L' then 'LTE' end bts_type,
			case when site_type='BS' then 'BSNL' When site_type='NB' then 'NBSNL' when site_type='IP' then 'IP' When site_type='US' then 'USO' ELSE 'UN-IDENTIFIED' end site_type ,
			g.outsrc_name 
			FROM (SELECT aa.* FROM ntmsdb.m_bts_master aa left JOIN (SELECT * from cnmcmob.mybts WHERE STATUS=0) ab ON aa.bts_id=ab.bts_id 
			where aa.circle_id='$circle_id' AND ab.bts_id IS NULL AND aa.e_p<>'P') b 
			LEFT JOIN ntmsdb.circle_master e ON b.circle_id=e.CIRCLE_ID 
			LEFT JOIN ntmsdb.ssa_master c ON b.ssa_id=c.SSA_ID LEFT JOIN ntmsdb.vendor_master d ON b.vendor_id=d.vendor_id
			LEFT JOIN ntmsdb.outsource_master g ON b.outsrc_id=g.outsrc_id ORDER BY b.bts_name asc";
			// print($sql);
		$res = mysqli_query($this->conn, $sql);
		while($row[] = mysqli_fetch_assoc($res));
		return array_filter($row);
	}


        function __destruct(){
                mysqli_close($this->conn);
        }
}
