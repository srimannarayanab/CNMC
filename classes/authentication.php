<?php
require_once __DIR__.'/../config/env.inc';

class userAuth{
    function __construct() {
        $this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysqli_connect_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysqli_connect_error());
    }
    
    function getUserDetails($msisdn, $circle_id){
        $sql ="select a.*, case when status='Y' then 'Active' when status='N' then 'Not-Active' when status='B' then 'Blocked' end user_status from cnmcmob.app_users a where msisdn like '$msisdn' and circle_id like '$circle_id'";
       //print($sql);
        $res = mysqli_query($this->conn, $sql);
        if(mysqli_affected_rows($this->conn)==0){
            return array('result'=>'false', 'remarks'=>'No user exsits');
        } else{
            $row[] = mysqli_fetch_assoc($res);
            return array('result'=>'true', 'remarks'=>$row[0]);
        }
    }
    
    function addUserDetails($imsi, $imei, $imsi_1, $imei_1, $firebase_id, $access_key, $msisdn_phone_state){
        $sql ="insert into cnmcmob.app_users(imsi, imei, imsi_1,imei_1,firebase_id, access_key, msisdn_phone_state) values 
				('$imsi','$imei','$imsi_1','$imei_1','$firebase_id','$access_key', '$msisdn_phone_state')";
        $res = mysqli_query($this->conn, $sql);
        if(mysqli_affected_rows($this->conn)==1){
            return array('result'=>'true', 'remarks'=>array("update_profile"=>'N'));
        } else{
            return array('result'=>'false','remarks'=> mysqli_error($this->conn));
        }
    }
    
    function updateUserDetails($name, $desg, $hrms, $msisdn, $email, $circle, $update_profile, $password, $lvl, $lvl2, $lvl3){
	$circle_master = $this->getCircleMaster();
	$circle_id = $circle_master[$circle];
		$sql = "update cnmcmob.app_users set name='$name', desg = '$desg', hrms_no='$hrms', email='$email',circle='$circle', circle_id='$circle_id', update_profile='$update_profile', password='$password' , lvl ='$lvl', lvl2='$lvl2', lvl3='$lvl3' where msisdn='$msisdn'";
		$res = mysqli_query($this->conn, $sql);
		if(mysqli_affected_rows($this->conn)==1){
			return array('result'=>'true', 'remarks'=>'user details updates sucessfully', 'Sql' =>"");
        } else{
            return array('result'=>'false','remarks'=> mysqli_error($this->conn));
        }
//		return $sql;
    }
	
    function addUser($name, $desg, $hrms, $email, $msisdn, $password, $circle, $access_key){
            $c_sql = "SELECT circle_id FROM ntmsdb.circle_master WHERE circle_name='$circle'";
            $c_res = mysqli_query($this->conn, $c_sql);
            while($r[] = mysqli_fetch_assoc($c_res));
            if(count($r)>0){
                    $circle_id = $r[0]['circle_id'];
                    if($circle_id =='ZZ'){
                            $user_privs ="co";
                    } else {
                            $user_privs ="circle";
                    }
            } else {
                    $circle_id = 'ZZ';
                    $user_privs = "circle";
            }

            $sql = "insert into cnmcmob.app_users(name, desg, hrms_no,email,msisdn,password,circle, circle_id, user_privs, access_key) values ('$name', '$desg', '$hrms', '$email', '$msisdn', '$password', '$circle', '$circle_id', '$user_privs', '$access_key')";
            $res = mysqli_query($this->conn, $sql);
            if(mysqli_affected_rows($this->conn)==1){
                    return array('result'=>'true', 'remarks'=>'User Created sucessfully', 'user_privs'=>$user_privs);
            } else{
                    return array('result'=>'false', 'remarks'=>mysqli_error($this->conn), 'sql'=>$sql);
            }
    }

    function addNewUser($name, $desg, $hrms, $email, $msisdn, $password, $circle, $ssaname, $user_type, $access_key){
        $c_sql = "SELECT circle_id FROM ntmsdb.circle_master WHERE circle_name='$circle'";
        $c_res = mysqli_query($this->conn, $c_sql);
        while($r[] = mysqli_fetch_assoc($c_res));
        if($user_type=='co'){
			if(count($r)>0){
				$circle_id = $r[0]['circle_id'];
				if($circle_id =='ZZ'){
						$user_privs ="co";
				} else {
						$user_privs ="circle";
				}
			} else {
				$circle_id = 'ZZ';
				$user_privs = "circle";
			}
        } else {
			$user_privs = $user_type;
			$circle_id = $r[0]['circle_id'];
        }

        $sql = "insert into cnmcmob.app_users(name, desg, hrms_no,email, msisdn, password, circle, ssaname, circle_id, user_privs, access_key) 
        values ('$name', '$desg', '$hrms', '$email', '$msisdn', '$password', '$circle', '$ssaname', '$circle_id', '$user_privs', '$access_key')";
        $res = mysqli_query($this->conn, $sql);
        if(mysqli_affected_rows($this->conn)==1){
                return array('result'=>'true', 'remarks'=>'User Created sucessfully', 'user_privs'=>$user_privs);
        } else{
                return array('result'=>'false', 'remarks'=>mysqli_error($this->conn), 'sql'=>$sql);
        }
}

    function getCircleDetails(){
            $sql = "SELECT circle_id,circle_name FROM ntmsdb.circle_master";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
//		print_r($row);
            $circle_master = array();
            foreach(array_filter($row) as $k){
                    $circle_master[$k['circle_name']] = $k['circle_id'];
            }	
            return $circle_master;
    }

    function getSSADetails(){
            $sql = "SELECT circle_id,ssa_id,ssa_name FROM ntmsdb.ssa_master where ssa_id not like '%ZZZ'";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return array_filter($row);
    }

    function verifyUserDetails($username, $password, $version_no, $firebase_id){
            $sql = "select * from cnmcmob.app_users where msisdn='$username' and password='$password' and status='Y'";
            $res = mysqli_query($this->conn, $sql);
            if(mysqli_affected_rows($this->conn)==1){
                    $appversion = $this->getAppVersion();
                    $faultmaster = $this->getFaultMaster();
                    $optr_master = $this->getOperatorNames();
                    while($row[] = mysqli_fetch_assoc($res));
		    $user_details = array_map(function($v){
        		return (is_null($v)) ? "" : $v;
    			},$row[0]);
                    $sql_1 = "update cnmcmob.app_users set last_login=sysdate(), app_version='$version_no', firebase_id='$firebase_id' where msisdn='$username'";
                    mysqli_query($this->conn, $sql_1);
                    
                    $circle_id = $row[0]['circle_id'];
                    $output = array("result"=>'true', "remarks"=>$user_details, "app_version"=>$appversion,
                            "fault_master"=>$faultmaster,"optr_master"=>$optr_master,
                            "error"=>mysqli_error($this->conn) ,'ssa_ids'=>$this->getSsaNames($circle_id) );
                    mysqli_query($this->conn, "insert into cnmcmob.users_logged_in(msisdn) values('$username')");
            } else{
                    $output = array("result"=>'false', "remarks"=>"User not exists/Blocked\nCheck Whether username and password entered correctly or the user is blocked contact circle administrator", "error"=>mysqli_error($this->conn));
            }
            return $output;
    }

    function getAppVersion(){
            $sql ="select max(version_no) app_version from cnmcmob.app_version";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return $row[0]['app_version'];
    }

    function forgotPassword($msisdn, $email, $password) {
            $sql = "update cnmcmob.app_users set password = '$password' where msisdn='$msisdn' and email='$email'";
        //     print($sql);
            $res = mysqli_query($this->conn, $sql);
            if(mysqli_affected_rows($this->conn)>=0){
                    $output = array('result'=>'true', 'remarks'=>'Reset sucessfully');
            } else {
                    $output = array('result'=>'false', 'remarks'=>'Check wheater the mobile number/Email exists or not');
            }
            return $output;
    }

    function getSsaNames($circle_id){
            $sql ="select ssa_id,ssa_name from ntmsdb.ssa_master where circle_id='$circle_id'";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return array_filter($row);
    }

    function getSsaBtsNames($ssa_id){
            $sql = "SELECT bts_id,replace(concat(REPLACE(REPLACE(bts_name,char(10),''), char(9), ''),'-',case when bts_type='G' then 'GSM'
when bts_type='U' then 'UMTS' when bts_type='L' then 'LTE' END),',',' ') bts_name FROM ntmsdb.m_bts_master WHERE ssa_id='$ssa_id' AND e_p='E' order by bts_name";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return array_filter($row);
    }

    function getFaultMaster(){
            $sql = "select * from ntmsdb.fault_master where fault_id not in (26) order by 2";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return array_filter($row);
    }

    function getOperatorNames(){
            $sql = "select * from ntmsdb.operator_master";
            $res = mysqli_query($this->conn, $sql);
            while($row[] = mysqli_fetch_assoc($res));
            return array_filter($row);
    }

    function enableDisableNotifications($notifications_status, $msisdn){
            $sql = "update cnmcmob.app_users set notifications='$notifications_status' where msisdn='$msisdn'";
            $res = mysqli_query($this->conn, $sql);
            $output= array();
            if(!mysqli_error($this->conn)) {
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;
    }

	function enableDisableSms($sms_status, $msisdn){
            $sql = "update cnmcmob.app_users set sms_notifications='$sms_status' where msisdn='$msisdn'";
            $res = mysqli_query($this->conn, $sql);
            $output= array();
            if(!mysqli_error($this->conn)) {
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;
    }
	function getUserDesignations(){
	    $sql = "select * from cnmcmob.user_designation";
            $res = mysqli_query($this->conn, $sql);
	    $desg = array();
	    while($row = mysqli_fetch_assoc($res)){
		array_push($desg, $row['designation']);
	    }
	    return $desg;

	}

	function getCircles(){
                $sql = "select * from ntmsdb.circle_master order by circle_id";
                $res = mysqli_query($this->conn, $sql);
                $circles = array();
                while($row = mysqli_fetch_assoc($res)){
                    array_push($circles, $row['CIRCLE_NAME']);
                }
                return $circles;
    
        }

	function getCircleSSAs($circle){
                $sql = "SELECT a.ssa_name FROM ntmsdb.ssa_master a , ntmsdb.circle_master b 
                WHERE a.CIRCLE_ID=b.circle_id AND b.CIRCLE_NAME='$circle' and ssa_name not LIKE 'UNIDENTIFIED%'
                ORDER BY ssa_id";
                $res = mysqli_query($this->conn, $sql);
                $ssas = array();
                while($row = mysqli_fetch_assoc($res)){
                    array_push($ssas, $row['ssa_name']);
                }
                return $ssas;
    
        }

    function updateUserLevel($desg, $lvl, $lvl2, $lvl3, $msisdn){
	$sql = "update cnmcmob.app_users set desg='$desg', lvl='$lvl', lvl2='$lvl2', lvl3='$lvl3' where msisdn='$msisdn'";
	$res = mysqli_query($this->conn, $sql);
        $output= array();
        if(!mysqli_error($this->conn)) {
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
        } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
        }
        return $output;
    }

     function getCircleMaster(){
	$sql = "select circle_id,circle_name from ntmsdb.circle_master";
	$res = mysqli_query($this->conn, $sql);
	while($row[] = mysqli_fetch_assoc($res));
	$circle_master = array();
	foreach(array_filter($row) as $k){
	   $circle_master[$k['circle_name']] = $k['circle_id'];	
	}
	return $circle_master;
	}
     function deactivateUser($msisdn){
	$sql ="update cnmcmob.app_users set status='B' where msisdn='$msisdn'";
	$res = mysqli_query($this->conn, $sql);
	$output= array();
            if(!mysqli_error($this->conn)) {
                    $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                    $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;

	}
     function activateUser($msisdn){
        $sql ="update cnmcmob.app_users set status='Y' where msisdn='$msisdn'";
        $res = mysqli_query($this->conn, $sql);
        $output= array();
            if(!mysqli_error($this->conn)) {
                    $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                    $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;

        }   

	function userPasswordReset($msisdn){
        $sql ="update cnmcmob.app_users set password=md5('bsnl@1234') where msisdn='$msisdn'";
	//print($sql);
        $res = mysqli_query($this->conn, $sql);
        $output= array();
            if(!mysqli_error($this->conn)) {
                    $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                    $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;
	
        }

	// function circle_administrator(){
	//   $sql="SELECT circle_id,name,desg,msisdn FROM cnmcmob.app_users WHERE admin='Y' ORDER BY 1";
	//   $res = mysqli_query($this->conn, $sql);
	//   while($row[]=mysqli_fetch_assoc($res));
	//   return array_filter($row);
	// }
        
        function deleteUser($msisdn){
            $sql ="insert into cnmcmob.app_users_deleted select * from cnmcmob.app_users where msisdn='$msisdn' limit 1;";
            $sql .="delete from cnmcmob.app_users where msisdn='$msisdn' limit 1;";
            $sql .="update cnmcmob.mybts set status=1, remarks='ThruApp' where msisdn='$msisdn'";
//            print($sql);
//            $res = mysqli_multi_query($this->conn, $sql);
            $output= array();     
            if(mysqli_multi_query($this->conn, $sql)){
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;
	}
	
	function userLevelUpdate($msisdn, $lvl, $lvl2, $lvl3){
	    $sql = "update cnmcmob.app_users set lvl='$lvl',lvl2='$lvl2', lvl3='$lvl3' where msisdn='$msisdn'";
	    #print($sql);
            $res = mysqli_query($this->conn, $sql);
            $output= array();
            if(!mysqli_error($this->conn)){
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;
        }
        
        function updateUserSSAName($ssaname, $username){
            $sql = "update cnmcmob.app_users set ssaname='$ssaname' where msisdn='$username'";
            $res = mysqli_query($this->conn, $sql);
            $output= array();
            if(!mysqli_error($this->conn)){
                $output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
            } else {
                $output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
            }
            return $output;            
        }

	function getCircleAdminstrators(){
	    $sql = "SELECT circle_id, name, msisdn, desg FROM cnmcmob.app_users WHERE admin='Y' AND STATUS='Y' ORDER BY circle_id, msisdn";
	    $res = mysqli_query($this->conn, $sql);
	    while($row[] = mysqli_fetch_assoc($res));
	    return array_filter($row);
	
        }
        function modifyUserType($msisdn, $user_privs, $circle_id){
		if($user_privs=="OutSrc"){
		  $user_privs = 'outSource';
		}
			$sql ="update cnmcmob.app_users set user_privs='$user_privs' where msisdn='$msisdn' and circle_id='$circle_id'";
			$res = mysqli_query($this->conn, $sql);
			$output= array();
			if(!mysqli_error($this->conn)){
					$output = array("result"=>"ok", "error"=>mysqli_error($this->conn), "sql"=>'');
			} else {
					$output = array("result"=>"fail", "error"=>mysqli_error($this->conn), "sql"=>'');
			}
			return $output;  
                
        }

        function verfiyRequest($api_key, $access_key, $msisdn){
			if($this->verifyAPIKey($api_key) and $this->verifyAccessKey($access_key, $msisdn)){
				return true;
			} else {
				return false;
			}
        }

        function verifyAPIKey($api_key){
			$sql ="select * from cnmcmob.api_keys where api_key='$api_key' and end_date>sysdate()";
			$res = mysqli_query($this->conn, $sql);
			if(!mysqli_error($this->conn)){
					if(mysqli_num_rows($res)>0){
							return true;
					} else {
							return false;
					}
			} else {
					return false;
			}
        }

        function verifyAccessKey($access_key, $msisdn){
			$sql ="select * from cnmcmoc.app_users where msisdn='$msisdn' and access_key='$access_key'";
			$res = mysqli_query($this->conn, $sql);
			if(!mysqli_error($this->conn)){
					if(mysqli_num_rows($res)>0){
							return true;
					} else {
							return false;
					}
			} else {
					return false;
			}

        }

    function __destruct(){
            mysqli_close($this->conn);
    }
}
