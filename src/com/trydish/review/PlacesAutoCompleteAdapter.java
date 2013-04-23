package com.trydish.review;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;


public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	private ArrayList<String> resultList;
	private static ArrayList<String> idList;
	private static final String LOG_TAG = "trydish";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	//?input=Vict&types=geocode&language=fr&sensor=true&key=AddYourOwnKeyHere

	//Should probably change this, shouldn't be stored on device, recommended to store on secure server
	private static final String API_KEY = "AIzaSyBlB6PKsmromS6TfMDIcy7fGRhnusRZ3r8";
	
	private double userLat;
	private double userLong;
	private int userRadius;

	public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public String getItem(int index) {
		return resultList.get(index);
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					// Retrieve the autocomplete results.
					System.out.println(constraint.toString());
					resultList = autocomplete(constraint.toString());

					// Assign the data to the FilterResults
					if (filterResults == null) {
						System.out.println("filterResults is null");
					} else {
						System.out.println("filterResults is NOT null");
					}
					if (resultList == null) {
						System.out.println("resultList is null");
					} else {
						System.out.println("resultList is NOT null");
					}
					//Log.d("filterResults is:", filterResults.toString());
					//Log.d("resultsList is:", resultList.toString());
					filterResults.values = resultList;
					filterResults.count = resultList.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				}
				else {
					notifyDataSetInvalidated();
				}
			}};
			return filter;
	}




	private ArrayList<String> autocomplete(String input) {
		ArrayList<String> resultList = null;

		//Log.d("initiating", "autocomplete");
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			//set all of the url components
			userLat = com.trydish.find.FindHome.getLat();
			userLong = com.trydish.find.FindHome.getLong();
			String intermediate = com.trydish.find.FindHome.getRadius();
			String[] inter = intermediate.split(" ");
			userRadius = Integer.parseInt(inter[0])*1609;
			System.out.println("This is the radius selected: " + Integer.toString(userRadius));
			
			StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?sensor=true&key=" + API_KEY);
			sb.append("&components=country:us");
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));
			//check for user location being null - should actually check for
			//if (userLat != 0.0 && userLong != 0.0) {
				sb.append("&radius=" + userRadius);
				sb.append("&location="+userLat+","+userLong);
			//}
			sb.append("&types=establishment");
			
			
 
			Log.d("key is:", sb.toString());
			
			URL url = new URL(sb.toString());
			//URL url = new URL("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=Amoeba&types=establishment&location=37.76999,-122.44696&radius=500&sensor=true&key=AIzaSyBlB6PKsmromS6TfMDIcy7fGRhnusRZ3r8");
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
			System.out.println("The results from the autocomplete were"+jsonResults.toString());
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
			Log.d("JSON results", jsonResults.toString());

			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
				//idList.add(predsJsonArray.getJSONObject(i).getString("id"));
			}
			//for (int i = 0; i < predsJsonArray.length(); i++) {
				//resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
				//idList.add(predsJsonArray.getJSONObject(i).getString("id"));
			//}
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Cannot process JSON results", e);
		}

		return resultList;
	}




	//public String getid(int index) {
	//	String toReturn = idList.get(index);
//		return toReturn;
//	}










}