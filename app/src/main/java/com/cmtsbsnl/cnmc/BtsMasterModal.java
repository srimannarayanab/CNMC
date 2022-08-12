package com.cmtsbsnl.cnmc;

import com.google.gson.annotations.SerializedName;

public class BtsMasterModal {

  @SerializedName("bts_id")
  private String bts_id;
  @SerializedName("bts_name")
	private String bts_name;
  @SerializedName("bts_type")
	private String bts_type;
  @SerializedName("ssa_id")
	private String ssa_id;
  @SerializedName("site_type")
  private String site_type;
  @SerializedName("operator_name")
	private String operator_name;

	public BtsMasterModal( String bts_id, String bts_name, String bts_type, String ssa_id, String site_type, String operator_name) {
		this.bts_id = bts_id;
		this.bts_name = bts_name;
		this.bts_type = bts_type;
		this.ssa_id = ssa_id;
    this.site_type = site_type;
		this.operator_name = operator_name;
	}

	public String getOperator_name() {
		return operator_name;
	}

  public String getSite_type() {
    return site_type;
  }

  public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getBts_id() {
		return bts_id;
	}

	public void setBts_id(String bts_id) {
		this.bts_id = bts_id;
	}

	public String getBts_name() {
		return bts_name;
	}

	public void setBts_name(String bts_name) {
		this.bts_name = bts_name;
	}

	public String getBts_type() {
		return bts_type;
	}

	public void setBts_type(String bts_type) {
		this.bts_type = bts_type;
	}

	public String getSsa_id() {
		return ssa_id;
	}

	public void setSsa_id(String ssa_id) {
		this.ssa_id = ssa_id;
	}

	@Override
	public String toString() {
		return bts_name+'-'+bts_type+'-'+ssa_id;
	}
}
