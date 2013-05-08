package com.trydish.main;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class global {
	public static int userID = -1;
	public static String username = "";
	public static ArrayList<String> allergy_ids;
	
	public static class DatabaseHandler extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "trydish_db";
		private static final String TABLE_ALLERGIES = "allergies";
		private static final String TABLE_DISHES = "dishes";
		private static final String TABLE_RESTAURANTS = "restaurants";
		private static SQLiteDatabase db;

		public DatabaseHandler(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLERGIES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANTS);
			onCreate(db);
		}

		public void execSQL(String commands){
			SQLiteDatabase db = this.getWritableDatabase();

			String[] result = commands.split(";");

			for (String stmt : result) {
				db.execSQL(stmt);
			}
		}

		public SQLiteDatabase getDB(){
			return this.getWritableDatabase();
		}
		
		public static String getAllergyName(String id) {
			Cursor c = db.query("allergies", new String[] {"name"}, "id=?", new String[] { id }, null, null, null, null);
			c.moveToFirst();
			return c.getString(0);
		}

		public void dropTables(){
			db = this.getWritableDatabase();
			db.execSQL("DROP TABLE IF EXISTS allergies");
			db.execSQL("DROP TABLE IF EXISTS dishes");
			db.execSQL("DROP TABLE IF EXISTS restaurants");
		}
	}
	
	public static String hash_pw(String password) {
		String hashed_pw = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String text = password;

			md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
			byte[] digest = md.digest();
			BigInteger bigInt = new BigInteger(1, digest);
	        hashed_pw = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return hashed_pw;
	}
}