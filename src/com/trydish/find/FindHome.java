package com.trydish.find;

import com.trydish.main.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

public class FindHome extends Fragment implements OnClickListener {
	
	//Implements OnClickListener so that we don't have to define onClick methods in the MainActivity
	
	//Keep track of what distance user has selected from drop down menu. Saving now b/c likely later passed to other funtion 
	private String searchDistance = "1 Mile";
	private static Context context;
	private View myView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_find_home,
				container, false);
	
		myView = view;
		context = view.getContext();
		//Note we have to call findViewById on the view b/c we are not in an Activity
		Spinner distanceSpinner = (Spinner) view.findViewById(R.id.distance_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.distance_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		distanceSpinner.setAdapter(adapter);
		
		//Create a new OnItemSelectedListener for the Spinner using anonymous class to define necessary methods
		OnItemSelectedListener listener = new OnItemSelectedListener() {
			
			//comment
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String itemSelected = (String) parent.getItemAtPosition(pos);
				searchDistance = itemSelected;
				Log.d("FindHome", searchDistance);
				//note that when this tab is selected, this method is executed
			}
			
			//comment
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		};
		//Set the spinners listener
		distanceSpinner.setOnItemSelectedListener(listener);
		
		//Grab the buttons and set their onClickListeners to be this Fragment
		ImageButton ib = (ImageButton) view.findViewById(R.id.search);
		Button b = (Button) view.findViewById(R.id.my_location);
		ib.setOnClickListener(this);
		b.setOnClickListener(this);
		
		GridView gridview = (GridView) view.findViewById(R.id.food_images);
	    gridview.setAdapter(new ImageAdapter(view.getContext()));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(context, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
		
		return view;

	}
	
	//Called when search icon is clicked
	public void searchClicked(View v) {
		//EditText et = (EditText) myView.findViewById(R.id.search_box);
		//et.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
		Log.d("Find Home", "search clicked");
	}
	
	//Called when the My Location button is clicked to change location
	public void myLocationClicked(View v) {
		Log.d("Find Home", "location clicked");
	}

	
	//Decides which method to call based on which button is clicked. Again, this is needed because by default buttons 
	//onClickListener is not the Fragment
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
		//search button clicked
		case R.id.search:
			searchClicked(arg0);
			break;
		//my location button clicked
		case R.id.my_location:
			myLocationClicked(arg0);
			break;
		}
		
	}

}
