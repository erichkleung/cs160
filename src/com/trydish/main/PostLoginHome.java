package com.trydish.main;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.trydish.main.global.DatabaseHandler;
import com.trydish.review.MapActivity;

public class PostLoginHome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Drawable bg = getResources().getDrawable(R.drawable.bluebartopsolid);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#151515")));
		
		Tab tab = actionBar.newTab()
				.setText(R.string.find)
				.setTabListener(new TabListener<com.trydish.find.FindHome>(
						this, "find", com.trydish.find.FindHome.class));
		actionBar.addTab(tab);

		tab = actionBar.newTab()
				.setText(R.string.review)
				.setTabListener(new TabListener<com.trydish.review.ReviewHome>(
						this, "review", com.trydish.review.ReviewHome.class));
		actionBar.addTab(tab);

		tab = actionBar.newTab()
				.setText(R.string.settings)
				.setTabListener(new TabListener<Settings>(
						this, "settings", Settings.class));
		actionBar.addTab(tab);
		
	}
	
	public void changeLocation(View v) {
		Log.d("Find Home", "CHANGE location clicked");
		
	}
	
	public void cancelChangeLocation(View v) {
		Log.d("Find Home", "CHANGE location clicked");
		
	}
	
	public void showMap(View v) {
		Intent intent = new Intent(com.trydish.find.FindHome.getContext(), MapActivity.class);
	    startActivity(intent);
	}


}
