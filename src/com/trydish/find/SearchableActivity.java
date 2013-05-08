package com.trydish.find;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.trydish.main.PostLoginHome;
import com.trydish.main.R;
import com.trydish.main.Settings;
import com.trydish.main.TabListener;

public class SearchableActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchable);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
//		 Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}
	}
	
	protected void doMySearch(String query) {
		
		Intent passQuery = new Intent(this, PostLoginHome.class);
		passQuery.putExtra("searchQuery", query);
		passQuery.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(passQuery);
		
		finish();
		
	}

}
