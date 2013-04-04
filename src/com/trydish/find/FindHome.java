package com.trydish.find;

import com.trydish.main.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FindHome extends Fragment {
	
	//Keep track of what distance user has selected from drop down menu. Saving now b/c likely later passed to other funtion 
	private String searchDistance = "1 Mile";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_find_home,
				container, false);
		
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

		return view;

	}

}
