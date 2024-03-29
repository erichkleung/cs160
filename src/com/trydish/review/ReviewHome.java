package com.trydish.review;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ImageButton;
import com.trydish.main.R;
import com.trydish.main.global;
import com.trydish.main.global.DatabaseHandler;

public class ReviewHome extends Fragment implements OnClickListener, OnItemClickListener {

	private View myView;
	private ActionBar actionBar;
	private static Context context;
	private int intentId = 800;
	private String reference;
	private ArrayList<String> dishes;

	private ArrayList<String> resultsFromPlaces;
	private String encodedImage = "";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.activity_review_home,
				container, false);
		myView = view;
		context = view.getContext();
		dishes = new ArrayList<String>();

		((Button)(view.findViewById(R.id.buttonDone))).setOnClickListener(this);
		((ImageButton)(view.findViewById(R.id.imageView1))).setOnClickListener(this);

		RestaurantDBTask task = new RestaurantDBTask();
		task.execute();

		EditText e = (EditText) myView.findViewById(R.id.editTextRestaurant);
		e.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) { }
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

		});

		//for autocomplete google API
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.editTextRestaurant);
		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(context, R.layout.list_item));
		autoCompView.setOnItemClickListener(this);

		return view;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//added call to super for compatibility 
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			if (data.getBooleanExtra("confirm", false)) {
				Toast toast = Toast.makeText(getActivity(), "Review submitted.", Toast.LENGTH_SHORT);
				toast.show();

				ViewGroup viewGroup = (ViewGroup)myView.findViewById(R.id.innerRelative);
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					View view = viewGroup.getChildAt(i);
					if (view instanceof EditText) {
						((EditText) view).setText("");
					} else if (view instanceof RatingBar) {
						((RatingBar) view).setRating(0);
					} else if (view instanceof AutoCompleteTextView) {
						((AutoCompleteTextView) view).setText("");
					}
				}
				
				viewGroup = (ViewGroup)myView.findViewById(R.id.innerRelative2);
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					View view = viewGroup.getChildAt(i);
					if (view instanceof EditText) {
						((EditText) view).setText("");
					} else if (view instanceof RatingBar) {
						((RatingBar) view).setRating(0);
					} else if (view instanceof AutoCompleteTextView) {
						((AutoCompleteTextView) view).setText("");
					}
				}
				FragmentManager manager = getActivity().getFragmentManager();
				manager.saveFragmentInstanceState(this);
				actionBar = getActivity().getActionBar();
				actionBar.setSelectedNavigationItem(0);
			} else if (requestCode == intentId) {
				//Using http://www.pocketmagic.net/2011/02/android-photopicker-using-intents-and-gallery/#.UXyyYCugkSQ as a model
				if (data != null) {
					Cursor cursor = getContext().getContentResolver().query(data.getData(), null, null, null, null);
					cursor.moveToFirst();  //if not doing this, 01-22 19:17:04.564: ERROR/AndroidRuntime(26264): Caused by: android.database.CursorIndexOutOfBoundsException: Index -1 requested, with a size of 1
					int idx = cursor.getColumnIndex(ImageColumns.DATA);
					String fileSrc = cursor.getString(idx); 

					Bitmap bitmapPreview = BitmapFactory.decodeFile(fileSrc); //load preview image
					BitmapDrawable bmpDrawable = new BitmapDrawable(Resources.getSystem(), bitmapPreview);
					((ImageButton)(myView.findViewById(R.id.imageView1))).setImageDrawable(bmpDrawable);

					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmapPreview.compress(Bitmap.CompressFormat.JPEG, 80, stream);
					byte[] array = stream.toByteArray();
					encodedImage = Base64.encodeToString(array, Base64.DEFAULT);

				}
			}
			//else do nothing
		}

	}

	public void done(View button) {
		EditText rText = (EditText)(myView.findViewById(R.id.editTextRestaurant));
		EditText nText = (EditText)(myView.findViewById(R.id.editTextName));
		EditText cText = (EditText)(myView.findViewById(R.id.editTextComments));
		RatingBar ratingBar = (RatingBar)(myView.findViewById(R.id.ratingBar));

		String restaurant = rText.getText().toString();
		String name = nText.getText().toString();
		String comments = cText.getText().toString();
		double rating = ratingBar.getRating();


		if (restaurant.equals("")) {
			Toast toast = Toast.makeText(getActivity(), "Please enter a restaurant.", Toast.LENGTH_SHORT);
			toast.show();
			return;
		} else if (name.equals("")) {
			Toast toast = Toast.makeText(getActivity(), "Please enter a dish name.", Toast.LENGTH_SHORT);
			toast.show();
			return;
		} else if (rating == 0) {
			Toast toast = Toast.makeText(getActivity(), "Please enter a rating.", Toast.LENGTH_SHORT);
			toast.show();
			return;
		} else if (resultsFromPlaces == null) {
			Toast toast = Toast.makeText(getActivity(), "Please use auto-complete for restauarant.", Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		Intent intent = new Intent(getActivity(), ConfirmReview.class);
		intent.putExtra("restaurant", restaurant);
		intent.putExtra("comments", comments);
		intent.putExtra("rating", rating);
		intent.putExtra("dish_name", name);
		intent.putExtra("restaurantID", 1);
		intent.putExtra("encodedImage", encodedImage);

		intent.putStringArrayListExtra("results from Places autocomplete detail request", resultsFromPlaces);

		startActivityForResult(intent, 1);
	}

	@Override
	public void onClick(View view) {
		if (view == myView.findViewById(R.id.buttonDone)) {
			done(view);
		}
		if (view == myView.findViewById(R.id.imageView1)) {
			addImage(view);
		}
	}

	public void onResume(View view) {

	}

	public static Context getContext() {
		return context;
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		String str = (String) adapterView.getItemAtPosition(position);
		String refToQuery = (String) ((PlacesAutoCompleteAdapter)adapterView.getAdapter()).getRef(position);
		reference = new String(refToQuery);
		placeTask pt = new placeTask();
		pt.execute();

	}

	public void addImage(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, intentId);
	}


	//task to grab restaurant details including location coordinates, id, address
	private class placeTask extends AsyncTask<Void, Void, ArrayList<String>> {

		ArrayList<String> resultList = null;
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();

		private final String endPoint = "https://maps.googleapis.com/maps/api/place/details/json?reference=" + reference + "&sensor=true&key=" + global.API_KEY;

		public ArrayList<String> doInBackground(Void... params)
		{

			try {
				URL url = new URL(endPoint);
				conn = (HttpURLConnection) url.openConnection();
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				// Load the results into a StringBuilder
				int read;
				char[] buff = new char[1024];
				while ((read = in.read(buff)) != -1) {
					jsonResults.append(buff, 0, read);
				}
			} catch (MalformedURLException e) {
				Log.e("trydish", "Error processing Places API URL", e);
				return resultList;
			} catch (IOException e) {
				Log.e("trydish", "Error connecting to Places API", e);
				return resultList;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			try {
				// Create a JSON object hierarchy from the results
				JSONObject jsonObj = new JSONObject(jsonResults.toString());
				JSONObject result = jsonObj.getJSONObject("result");

				resultList = new ArrayList<String>();

				//use
				String name = result.getString("name");
				resultList.add(name);

				JSONObject latLongJson = result.getJSONObject("geometry");
				JSONObject latLong = latLongJson.getJSONObject("location");
				String latitude = latLong.getString("lat");
				String longitude = latLong.getString("lng");
				resultList.add(latitude);
				resultList.add(longitude);

				//use
				Double latDouble =  Double.parseDouble(latitude);
				Double lngDouble =  Double.parseDouble(longitude);

				JSONArray addressJsonArray = result.getJSONArray("address_components");

				//use
				String cityName = "";
				String stateName = "";
				String zip = "";

				String temp;
				for (int i = 0; i < addressJsonArray.length(); i++) {
					temp = addressJsonArray.getJSONObject(i).getString("short_name");
					if (i == 2) {
						cityName = temp;
					} else if (i == 3) {
						stateName = temp;
					} else if (i == 5) {
						zip = temp;
					}

				}

				String formatted_address = result.getString("formatted_address");
				String[] separatedAddress = formatted_address.split(",");
				//use
				String line1 = name;
				String line2 = "";
				String line3 = "";
				try {
					line2 = separatedAddress[0];
					line3 = separatedAddress[1] + "," + separatedAddress[2] + "," + separatedAddress[3];
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.d("trydish", "address in not expected format");
				}
				resultList.add(line1);
				resultList.add(line2);
				resultList.add(line3);
				resultList.add(cityName);
				resultList.add(stateName);
				resultList.add(zip);


				//use
				String formatted_phone_number = result.getString("formatted_phone_number");
				String id = result.getString("id");
				resultList.add(formatted_phone_number);
				resultList.add(id);
			} catch (JSONException e) {
				System.out.println("catch");
				Log.e("trydish", "Cannot process JSON results", e);
			}

			return resultList;

		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			updateArray(result);
		}
	}

	private class RestaurantDBTask extends AsyncTask<Void, Void, SQLiteDatabase> {

		protected SQLiteDatabase doInBackground(Void...arg0) {			
			String url = "http://trydish.pythonanywhere.com/sync_restaurants";
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
					db_handler.dropTables();
					db_handler.execSQL(databaseCommands);

				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					System.out.println("status: " + response.getStatusLine().getStatusCode());
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return db;
		}

		@Override
		protected void onPostExecute(SQLiteDatabase db) {
			storeRestDB(db);
		}
	}


	private void storeRestDB(SQLiteDatabase db) {		
		DishDBTask task = new DishDBTask();
		task.execute();
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
					System.out.println("status: " + response.getStatusLine().getStatusCode());
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return db;
		}

		@Override
		protected void onPostExecute(SQLiteDatabase db) {
			storeDishDB(db);
		}
	}


	private void storeDishDB(SQLiteDatabase db) {		
		AllergyDBTask task = new AllergyDBTask();
		task.execute();
	}


	private class AllergyDBTask extends AsyncTask<Void, Void, SQLiteDatabase> {

		protected SQLiteDatabase doInBackground(Void...arg0) {			
			String url = "http://trydish.pythonanywhere.com/sync_allergies";
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
					System.out.println("status: " + response.getStatusLine().getStatusCode());
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return db;
		}

		@Override
		protected void onPostExecute(SQLiteDatabase db) {
		}
	}

	public void updateArray(ArrayList<String> results) {
		this.resultsFromPlaces = results;
		dishes = new ArrayList<String>();
		String query = "SELECT * FROM restaurants WHERE google_id=" + '"' + resultsFromPlaces.get(10).toString() + '"';
		String restID = null;
		DatabaseHandler dbHandler = new global.DatabaseHandler(getContext());
		SQLiteDatabase db = dbHandler.getDB();
		Cursor cs = db.rawQuery(query, null);
		if (cs.moveToFirst()) {
			do {
				//				System.out.println(cs.getString(1));
				//				dishes.add(cs.getString(1));
				restID = cs.getString(0);
			} while (cs.moveToNext());
		}
		if (restID != null) {
			query = "SELECT * FROM dishes WHERE restaurant=" + restID;
			cs = db.rawQuery(query, null);
			if (cs.moveToFirst()) {
				do {
					dishes.add(cs.getString(1));
				} while (cs.moveToNext());
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, dishes);
		AutoCompleteTextView textView = (AutoCompleteTextView) myView.findViewById(R.id.editTextName);
		textView.setThreshold(1);
		textView.setAdapter(adapter);
	}

}
