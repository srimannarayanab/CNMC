package com.cmtsbsnl.cnmc;

public class MyBtsModal {
	private String bts_name, bts_type, ssa_id;
	private int mybts_id;

	public MyBtsModal(String bts_name, String bts_type, String ssa_id, int mybts_id) {
		this.bts_name = bts_name;
		this.bts_type = bts_type;
		this.ssa_id = ssa_id;
		this.mybts_id = mybts_id;

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

	public int getMybts_id() {
		return mybts_id;
	}

	@Override
	public String toString() {
		return bts_name + '\\' +bts_type + '\\'+ ssa_id;
	}
}
