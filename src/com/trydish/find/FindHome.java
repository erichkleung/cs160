package com.trydish.find;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Spinner;

import com.trydish.main.R;
import com.trydish.main.global;
import com.trydish.main.global.DatabaseHandler;



public class FindHome extends Fragment implements OnClickListener {

	//Implements OnClickListener so that we don't have to define onClick methods in the LoginHome
	//PopupWindow usage modeled after http://www.mobilemancer.com/2011/01/08/popup-window-in-android/ 

	//Keep track of what distance user has selected from drop down menu. Saving now b/c likely later passed to other function 
	private static String searchDistance = "1 mile";
	private static Context context;
	private View myView;
	private LocationManager manager;
	private Location location;
	private static double latitude;
	private static double longitude;
	private PopupWindow pop;
	private String changedLocation;
	private static boolean myLocationChanged = false;
	private ArrayList<String> dishes;
	private SearchView searchView;


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
		Button b = (Button) view.findViewById(R.id.my_location);
		b.setOnClickListener(this);

		// PROCESS SEARCH!
		Intent getSome = getActivity().getIntent();

		if (getSome != null) {
			String str = getSome.getStringExtra("searchQuery");
			if (str != null) {
				// start fragment for search in here...
			} else {
				GridView gridview = (GridView) view.findViewById(R.id.food_images);
				gridview.setAdapter(new ImageAdapter(view.getContext(), ImageAdapter.HALF));

				gridview.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						SearchView et = (SearchView) myView.findViewById(R.id.search_box);
						imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

						Fragment view_dish = new ViewDish();
						FragmentTransaction trans = getFragmentManager().beginTransaction();

						trans.replace(((ViewGroup) myView.getParent()).getId(), view_dish);
						trans.addToBackStack(null);
						trans.commit();
					}
				});
			}
		}

		//Hide keyboard
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (myLocationChanged == false) {
			setLocation();
		}

		// Setting up the SearchView widget
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) view.findViewById(R.id.search_box);
		ComponentName cn = new ComponentName("com.trydish.main", "com.trydish.find.SearchableActivity");
		searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

		DishDBTask dbTask = new DishDBTask();
		dbTask.execute();
		
		return view;

	}

	public void setLocation() {
		String providerName = manager.getBestProvider(new Criteria(), true);
		if (providerName == null) {
			System.out.println("provider is null");}
		location = manager.getLastKnownLocation(providerName);
		System.out.println("location object is: "+location);

		Button location_button = (Button) myView.findViewById(R.id.my_location);
		location_button.setText("My Location");

		if (location == null) {
			location_button.setText("GPS Off");
		} else {
			latitude = location.getLatitude();
			longitude = location.getLongitude();

			try {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
				boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected(); 

				if (isConnected) {
					Geocoder myLocation = new Geocoder(context.getApplicationContext(), Locale.getDefault());
					List<Address> myList = myLocation.getFromLocation(latitude, longitude, 1);
					int address_lines = myList.get(0).getMaxAddressLineIndex();

					if (address_lines >= 1) {
						String address = myList.get(0).getAddressLine(address_lines - 1);
						String city = address.split(",", 2)[0];
						location_button.setText(city);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Called when the My Location button is clicked to change location
	//PopupWindow usage modeled after http://www.mobilemancer.com/2011/01/08/popup-window-in-android/ 
	public void myLocationClicked(View v) {
		Log.d("Find Home", "location clicked");
		//Add the popup window
		LayoutInflater inflater = LayoutInflater.from(context);
		final View popup_menu = inflater.inflate(R.layout.location_popup, (ViewGroup) myView.findViewById(R.id.location_popup));
		pop = new PopupWindow(popup_menu, 700, 300, true);
		pop.showAtLocation(popup_menu, Gravity.NO_GRAVITY, 50, 160);
		//grab button and set onClickListener to be able to dismiss the window
		Button b = (Button) popup_menu.findViewById(R.id.change_my_location);

		//set b's OnClickListener to allow dismissing popup
		b.setOnClickListener(new OnClickListener() {
			@Override	
			public void onClick(View v) {
				//do something
				EditText et = (EditText) popup_menu.findViewById(R.id.change_location_edit);
				changedLocation = et.getText().toString();
				changeLocationTask clk = new changeLocationTask(); 
				clk.execute(changedLocation);
				myLocationChanged = true;
				pop.dismiss();
			}
		});

		Button c = (Button) popup_menu.findViewById(R.id.cancel_change_my_location);
		c.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//get rid of popup window
				pop.dismiss();
			}
		});


	}


	//Decides which method to call based on which button is clicked. Again, this is needed because by default buttons 
	//onClickListener is not the Fragment
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
		//search button clicked

		case R.id.my_location:
			myLocationClicked(arg0);
			break;
		}

	}



	private class changeLocationTask extends AsyncTask<String, Void, Address> {

		public Address doInBackground(String... params)
		{

			//used reference: http://stackoverflow.com/questions/4873076/get-latitude-and-longitude-from-city-name
			Geocoder coder = new Geocoder(context.getApplicationContext());
			List<Address> address;
			try 
			{
				address = coder.getFromLocationName(changedLocation,5);
				if (address == null) {
					Log.d("trydish", "############Address not correct #########");
				}
				Address location = address.get(0);

				Log.d("trydish", "Address Latitude : "+ location.getLatitude() + "Address Longitude : "+ location.getLongitude());
//				System.out.println("latitude and longitude is: " + location.getLatitude() + " " + location.getLongitude());

				return location;
			}
			catch(Exception e)
			{
				System.out.println(e);
				Log.d("trydish", "MY_ERROR : ############Address Not Found");
				//return "no address";
				return null;
			}

		}

		//@Override
		protected void onPostExecute(Address results)
		{       
			if(results != null)

			{   
				try {
					changeLocation(results);
				}
				catch (Exception ex) {
					//do something
				}
			}
		}

	}

	public void changeLocation(Address a) {
		Button location_button = (Button) myView.findViewById(R.id.my_location);
		location_button.setText(changedLocation);
		setLat(location.getLatitude());
		setLong(location.getLongitude());
	}




	public static Context getContext() {
		return context;
	}

	public static String getRadius() {
		return searchDistance;
	}

	public static double getLat() {
		return latitude;
	}

	public static double getLong() {
		return longitude;
	}
	public static void setLat(double lat) {
		latitude = lat;
	}

	public static void setLong(double lng) {
		longitude = lng;
	}
	
	private class DishDBTask extends AsyncTask<Void, Void, SQLiteDatabase> {

		protected SQLiteDatabase doInBackground(Void...arg0) {			
			String url = "http://trydish.pythonanywhere.com/sync_dishes";
			SQLiteDatabase db = null;

			HttpResponse response;
			HttpClient httpclient = new DefaultHttpClient();

			try {
				response = httpclient.execute(new HttpGet(url));
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);

					final String databaseCommands = out.toString();
					out.close(); 

					DatabaseHandler db_handler = new DatabaseHandler(context);
					db_handler.execSQL(databaseCommands);

				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
//					System.out.println("status: " + response.getStatusLine().getStatusCode());
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return db;
		}

		@Override
		protected void onPostExecute(SQLiteDatabase db) {
			updateArray();
		}
	}
	
	public void updateArray() {
		dishes = new ArrayList<String>();
		String query = "SELECT id as _id,name FROM dishes";
		DatabaseHandler dbHandler = new global.DatabaseHandler(getContext());
		SQLiteDatabase db = dbHandler.getDB();
		Cursor cs = db.rawQuery(query, null);
		CursorAdapter cursor = null;
		if (cs.moveToFirst()) {
			do {
				System.out.println(cs.getString(1));
			} while (cs.moveToNext());
		}
	}
	

}
