package com.trydish.main;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.Tab;

public class PostLoginHome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
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

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_post_login_home, menu);
//		return true;
//	}

}
