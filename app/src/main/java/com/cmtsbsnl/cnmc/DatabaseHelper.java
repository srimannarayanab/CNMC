package com.cmtsbsnl.cnmc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String BTSMASTER_TABLE ="m_bts_master";
	public static final String ID = "m_bts_id";
	public static final String BTS_ID="bts_id";
	public static final String BTS_NAME="bts_name";
	public static final String BTS_TYPE="bts_type";
  public static final String SITE_TYPE="site_type";
	public static final String SSA_ID="ssa_id";
	public static final String OPERATOR_NAME="operator_name";
	public static final String TAG="Database Helper";

	public DatabaseHelper(@Nullable Context context) {
		super(context, "cnmc.db",null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
//		sqLiteDatabase.execSQL("drop table if exists "+BTSMASTER_TABLE);
		String createTableStatement = "CREATE TABLE " + BTSMASTER_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BTS_ID + " TEXT, " + BTS_NAME + " TEXT, " + BTS_TYPE + " TEXT,"+ SSA_ID + " TEXT,"+ SITE_TYPE +" TEXT,"+ OPERATOR_NAME + " TEXT)";
		sqLiteDatabase.execSQL(createTableStatement);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}

	public boolean addOne(BtsMasterModal bm){
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(BTS_NAME, bm.getBts_name());
		cv.put(BTS_TYPE, bm.getBts_type());
		cv.put(BTS_ID , bm.getBts_id());
		cv.put(SSA_ID, bm.getSsa_id());
    cv.put(SITE_TYPE, bm.getSite_type());
		cv.put(OPERATOR_NAME, bm.getOperator_name());
		long insert = sqLiteDatabase.insert(BTSMASTER_TABLE, null, cv);
		sqLiteDatabase.close();
		if(insert==-1){
			return false;
		} else{
			return true;
		}
	}

	public void addAll(List<BtsMasterModal> bts_data){
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		sqLiteDatabase.execSQL("DELETE FROM "+ BTSMASTER_TABLE);
		for(BtsMasterModal x :bts_data){
			ContentValues cv = new ContentValues();
			cv.put(BTS_NAME, x.getBts_name());
			cv.put(BTS_TYPE, x.getBts_type());
			cv.put(BTS_ID , x.getBts_id());
			cv.put(SSA_ID, x.getSsa_id());
      cv.put(SITE_TYPE, x.getSite_type());
			cv.put(OPERATOR_NAME, x.getOperator_name());
			long insert = sqLiteDatabase.insert(BTSMASTER_TABLE, null, cv);
		}
		sqLiteDatabase.close();
		Log.d(TAG, "insertion completed");

	}

	public void dropTable(){
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		sqLiteDatabase.execSQL("drop table if exists m_bts_master");
	}


	public List<BtsMasterModal> getEveryOne(){
		List<BtsMasterModal> returnList = new ArrayList<>();
		String queryString = "SELECT * from "+BTSMASTER_TABLE;
		SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery(queryString, null);
		if(cursor.moveToFirst()){
			do{
				int id = cursor.getInt(0);
				String btsId = cursor.getString(1);
				String btsName = cursor.getString(2);
				String btsType = cursor.getString(3);
				String ssaId = cursor.getString(4);
        String siteType = cursor.getString(5);
				String operatorName = cursor.getString(5);
				BtsMasterModal btsMasterModal = new BtsMasterModal( btsId, btsName, btsType, ssaId , siteType, operatorName);
				returnList.add(btsMasterModal);
			} while(cursor.moveToNext());
		} else {

		}
		cursor.close();
		sqLiteDatabase.close();
		return returnList;
	}

  public List<IpVendorModel> getIpvendors(){
    List<IpVendorModel> ipVendorModelList = new ArrayList<>();
    String queryString = "SELECT DISTINCT OPERATOR_NAME from m_bts_master where operator_name<>'Not Applicable' and site_type='IP'";
    SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery(queryString, null);
    if(cursor.moveToFirst()){
      do {
        String ip_vendor_name = cursor.getString(0);
        IpVendorModel ipVendorModel = new IpVendorModel(ip_vendor_name);
        ipVendorModelList.add(ipVendorModel);
      }while(cursor.moveToNext());
    } else{

    }
    cursor.close();
    sqLiteDatabase.close();
    return ipVendorModelList;
  }

  public List<SSAIdsModel> getSSAIds(){
    List<SSAIdsModel> ssaIds = new ArrayList<>();
    String queryString = "SELECT DISTINCT SSA_ID FROM M_BTS_MASTER order by ssa_id";
    SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery(queryString, null);
    if(cursor.moveToFirst()){
      do{
        String ssa_id = cursor.getString(0);
        SSAIdsModel ssaIdsModel = new SSAIdsModel(ssa_id);
        ssaIds.add(ssaIdsModel);
      }while(cursor.moveToNext());
    } else {

    }
    cursor.close();
    sqLiteDatabase.close();
    return ssaIds;
  }

  public List<BtsMasterModal> getBtsList(String operator_name, String ssa_id){
    List<BtsMasterModal> btsMasterModalList = new ArrayList<>();
    String queryString = "SELECT bts_id, bts_name, bts_type, ssa_id, site_type, operator_name from M_BTS_MASTER where operator_name=? and ssa_id=?";
    String[] args = new String[] {operator_name, ssa_id};
    SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery(queryString, args);
    if(cursor.moveToFirst()){
      do{
        String btsid = cursor.getString(0);
        String btsname = cursor.getString(1);
        String btstype = cursor.getString(2);
        String ssaid = cursor.getString(3);
        String sitetype = cursor.getString(4);
        String operatorname = cursor.getString(5);
        BtsMasterModal btsMasterModal = new BtsMasterModal(btsid, btsname, btstype,ssaid, sitetype, operatorname);
        btsMasterModalList.add(btsMasterModal);

      }while(cursor.moveToNext());
    } else {

    }
    cursor.close();
    sqLiteDatabase.close();
    return btsMasterModalList;
  }
}
