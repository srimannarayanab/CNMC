package com.cmtsbsnl.cnmc;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MyInterface {

  @Headers("Content-Type: application/json")
  @POST("IpAddsites")
  Call<ApiResponse> IpAddSites(@Body String input, @Header("Authorization") String webToken);

  @Headers("Conten-Type: application/json")
  @POST("GetIpAssignedSites")
  Call<ApiResponse> GetIpAssignedSites(@Body String input, @Header("Authorization") String webToken);

  @GET("UnlinkIPAssignedBts")
  Call<ApiResponse> UnlinkIPAssignedBts(@Header("Authorization") String webToken, @Query("username") String username, @Query("msisdn") String msisdn, @Query("bts_id") String bts_id);

  @GET("test1")
  Call<BtsMasterModal> getBtsModel();

  @Headers("Content-Type: application/json")
  @POST("getBtsDetails")
  Call<ApiResponse> getBtsDetails(@Header("Authorization") String webToken, @Body String input);

  @Headers("Content-Type: application/json")
  @POST("changeIpTechnicianNo")
  Call<ApiResponse> changeIpTechnician(@Header("Authorization") String webToken, @Body String input);

  @Headers("Content-Type: application/json")
  @POST("addIpUser")
  Call<ApiResponse> addIpUser(@Header("Authorization") String webToken, @Body String input);

}
