package com.cmtsbsnl.cnmc;

import com.google.gson.annotations.SerializedName;

public class SSAIdsModel {

  @SerializedName("ssa_id")
  private String ssa_id;

  public SSAIdsModel(String ssa_id) {
    this.ssa_id = ssa_id;
  }

  public String getSsa_id() {
    return ssa_id;
  }
}
