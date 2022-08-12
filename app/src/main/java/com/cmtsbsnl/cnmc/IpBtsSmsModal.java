package com.cmtsbsnl.cnmc;

public class IpBtsSmsModal {
  private String bts_id;
  private String bts_name;
  private String bts_type;
  private String ssa_id;
  private String operator_name;
  private String add_by;
  private String add_date;

  public IpBtsSmsModal(String bts_id, String bts_name, String bts_type, String ssa_id, String operator_name, String add_by, String add_date) {
    this.bts_id = bts_id;
    this.bts_name = bts_name;
    this.bts_type = bts_type;
    this.ssa_id = ssa_id;
    this.operator_name = operator_name;
    this.add_by = add_by;
    this.add_date = add_date;
  }

  public String getBts_id() {
    return bts_id;
  }

  public String getBts_name() {
    return bts_name;
  }

  public String getBts_type() {
    return bts_type;
  }

  public String getSsa_id() {
    return ssa_id;
  }

  public String getOperator_name() {
    return operator_name;
  }


  public String getAdd_by() {
    return add_by;
  }

  public String getAdd_date() {
    return add_date;
  }

  @Override
  public String toString() {
    return bts_name + '\\' + bts_type + '\\' + operator_name;
  }
}
