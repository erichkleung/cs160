package com.trydish.main;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SearchView;

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
//		actionBar.setBackgroundDrawable(bg);
		
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
	
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    // Inflate the options menu from XML
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.activity_post_login_home, menu);
//	    
//	    // Get the SearchView and set the searchable configuration
//	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//		ComponentName cn = new ComponentName("com.trydish.main", "com.trydish.find.SearchableActivity");
//		searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
//	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//	    
//	    return super.onCreateOptionsMenu(menu);
//	}
	
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
