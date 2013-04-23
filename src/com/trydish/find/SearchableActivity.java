package com.trydish.find;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.trydish.main.R;

public class SearchableActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchable);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}
	}
	
	protected void doMySearch(String query) {
		
		GridView gridview = (GridView) findViewById(R.id.search_food_images);
		gridview.setAdapter(new ImageAdapter(this, ImageAdapter.HALF));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Fragment view_dish = new ViewDish();
				FragmentTransaction trans = getFragmentManager().beginTransaction();

				trans.replace(R.id.search_results, view_dish);
				trans.addToBackStack(null);
				trans.commit();
			}
		});
		
	}

}
