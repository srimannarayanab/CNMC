<?php
require_once __DIR__.'/../config/env.inc';

class AppUsers {
    function __construct() {
        $this->conn = @mysqli_connect(SERVER, USERNAME, PASSWORD) or die('Connection error -> ' . mysqli_connect_error());
        mysqli_select_db($this->conn, DATABASE) or die('Database error -> ' . mysqli_connect_error());
    }
    
    function circleWiseUsers(){
        $sql = "SELECT aaa.*,bbb.site_count FROM (
            SELECT ab.*,ac.total_users FROM (SELECT aa.*,bb.only_registered FROM (
            SELECT circle,COUNT(DISTINCT a.msisdn) registered_bts_count, COUNT(DISTINCT b.bts_id) bts_enable
             FROM app_users a , mybts b where a.msisdn=b.msisdn AND b.STATUS=0
            GROUP BY circle) aa,(
            SELECT circle, COUNT(msisdn) user_cnt,
            SUM(case when last_login IS NULL then 1 ELSE 0 END) only_registered FROM app_users a 
            GROUP BY circle ) bb where aa.circle=bb.circle) ab,
            (SELECT circle,COUNT(*) total_users FROM app_users GROUP BY circle) ac
            WHERE ab.circle=ac.circle) aaa , (SELECT a.circle_id,b.circle_name,COUNT(*) site_count FROM ntmsdb.m_bts_master a, ntmsdb.circle_master b WHERE e_p='E' AND a.circle_id =b.CIRCLE_ID GROUP BY a.circle_id) bbb
            WHERE aaa.circle=bbb.circle_name";
        $res = mysqli_query($this->conn, $sql);
        while($row[] = mysqli_fetch_assoc($res));
        return $row;
    }
    
    function ssawiswUsers($circle_id){
        $sql = "SELECT circle_id,ssa_id,COUNT(bts_id) TotalBts,SUM(case when configured='Configured' then 1 ELSE 0 END) configured,
                SUM(case when configured='Not-Configured' then 1 ELSE 0 END) not_configured FROM (
                SELECT circle_id,ssa_id,bts_id,configured FROM (
                SELECT circle_id,ssa_id,aa.bts_id,case when bb.bts_id IS NULL then 'Not-Configured' ELSE 'Configured' END configured FROM ntmsdb.m_bts_master aa LEFT JOIN 
                (SELECT DISTINCT bts_id bts_id FROM cnmcmob.mybts) bb
                on aa.bts_id=bb.bts_id)  a) ab
                GROUP BY circle_id,ssa_id";
        $res = mysqli_query($this->conn, $sql);
        while($row[] = mysqli_fetch_assoc($res));
        return $row;           
    } 
}