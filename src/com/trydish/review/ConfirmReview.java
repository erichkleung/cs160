package com.trydish.review;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trydish.main.R;
import com.trydish.main.global;

public class ConfirmReview extends Activity {

	Intent intent;
	ActivityResult actResult;
	String restaurant, dish;
	ArrayList<String> safe_allergies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_review);
		intent = getIntent();

		((TextView)findViewById(R.id.textViewRestaurant)).setText("Restaurant: " + intent.getStringExtra("restaurant"));
		((TextView)findViewById(R.id.textViewName)).setText("Dish Name: " + (intent.getStringExtra("name")));
		
		System.out.println("working");
		
		if (global.allergy_ids != null) {
			for (String allergyID : global.allergy_ids) {
				System.out.println("allergyID: " + allergyID);
				String allergyName = global.DatabaseHandler.getAllergyName(allergyID);
				
				LinearLayout allergiesList = (LinearLayout) findViewById(R.id.Layout);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		    	params.setMargins(13, 13, 13, 13);
		    	CheckBox allergy = new CheckBox(this);
		    	allergy.setText(allergyName);
		    	allergy.setChecked(true);
		    	allergy.setTextColor(Color.WHITE);
		    	allergy.setTextSize(20);
		    	allergiesList.addView(allergy, params);
			}
		} else {
			LinearLayout allergiesList = (LinearLayout) findViewById(R.id.Layout);
			TextView noAllergies = new TextView(this);
			noAllergies.setTextColor(Color.WHITE);
			noAllergies.setTextSize(20);
			noAllergies.setText("(no known allergies)");
			allergiesList.addView(noAllergies);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_confirm_review, menu);
		return true;
	}

	public void cancel(View view) {
		Intent result = new Intent();
		result.putExtra("confirm", false);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	public void confirm(View view) {
		ProgressBar progress = (ProgressBar)findViewById(R.id.review_progressbar);
		progress.setVisibility(View.VISIBLE);
		
		safe_allergies = new ArrayList<String>();
		LinearLayout allergiesList = (LinearLayout) findViewById(R.id.Layout);
		for (int i=0; i < allergiesList.getChildCount(); i++) {
		      CheckBox v = (CheckBox)allergiesList.getChildAt(i);
		      if (v.isChecked()) {
		    	  safe_allergies.add(v.getText().toString().toLowerCase());
		      }
		}

		AddRestaurantTask addRestaurant = new AddRestaurantTask();
		ArrayList<String> placesStuff = intent.getStringArrayListExtra("results from Places autocomplete detail request");
		addRestaurant.execute(placesStuff.get(0), placesStuff.get(1),placesStuff.get(2),placesStuff.get(3),placesStuff.get(4),placesStuff.get(5),placesStuff.get(6),placesStuff.get(7),placesStuff.get(8),placesStuff.get(9),placesStuff.get(10));
		
	}

	public void confirm2(int id) {
		if (intent.getIntExtra("dishID", -1) != -1) {
			addReview(intent.getIntExtra("dishID",-1));
		} else {
			AddDishTask addDish = new AddDishTask();
			addDish.execute(intent.getStringExtra("name"), ""+id);
		}
	}

	private class AddReviewTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			String url = "http://trydish.pythonanywhere.com/add_review";
			String responseString;

			HttpClient httpclient = new DefaultHttpClient();

			HttpPost post = new HttpPost(url);
			try {
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("dish", params[0]));
				postParameters.add(new BasicNameValuePair("author", params[1]));
				postParameters.add(new BasicNameValuePair("rating", params[2]));
				postParameters.add(new BasicNameValuePair("comment", params[3]));
				postParameters.add(new BasicNameValuePair("encodedImage", params[4]));
				
				for (int i = 0; i < safe_allergies.size(); i++) {
					postParameters.add(new BasicNameValuePair("not_present", safe_allergies.get(i)));
				}
				
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
				HttpResponse response = httpclient.execute(post);

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else {
					//Closes the connection.
					System.out.println("Status: " + response.getStatusLine().getStatusCode());
					response.getEntity().getContent().close();
					return false;
				}
			} catch (Exception e) {
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean callsubmit) {
			if (callsubmit) {
				submitFinished();
			}
		}
	}

	private class AddDishTask extends AsyncTask<String, Void, Integer> {

		//params: dish (id), user (id), rating, comment
		@Override
		protected Integer doInBackground(String... params) {
			JSONObject result;
			String url = "http://trydish.pythonanywhere.com/add_dish";
			String responseString;

			HttpClient httpclient = new DefaultHttpClient();

			HttpPost post = new HttpPost(url);
			try {
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("name", params[0]));
				postParameters.add(new BasicNameValuePair("restaurant", params[1]));

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
				HttpResponse response = httpclient.execute(post);

				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
					result = new JSONObject(responseString);
				} else {
					//Closes the connection.
					System.out.println("Status: " + response.getStatusLine().getStatusCode());
					response.getEntity().getContent().close();
					return -1;
				}
			} catch (Exception e) {
				return -1;
			}

			try {
				return (Integer) result.get("id");
			} catch (JSONException e) {
				e.printStackTrace();
				return -1;
			}
		}

		protected void onPostExecute(Integer id) {
			addReview(id);
		}
	}

	private class AddRestaurantTask extends AsyncTask<String, Void, Integer> {

		//params: dish (id), user (id), rating, comment
		@Override
		protected Integer doInBackground(String... params) {
			JSONObject result;
			String url = "http://trydish.pythonanywhere.com/add_restaurant";
			String responseString;

			HttpClient httpclient = new DefaultHttpClient();

			HttpPost post = new HttpPost(url);
			try {
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("name", params[0]));
				postParameters.add(new BasicNameValuePair("lat", params[1]));
				postParameters.add(new BasicNameValuePair("long", params[2]));
				postParameters.add(new BasicNameValuePair("address_line_1", params[3]));
				postParameters.add(new BasicNameValuePair("address_line_2", params[4]));
				postParameters.add(new BasicNameValuePair("address_line_3", params[5]));
				postParameters.add(new BasicNameValuePair("city", params[6]));
				postParameters.add(new BasicNameValuePair("state", params[7]));
				postParameters.add(new BasicNameValuePair("zip", params[8]));
				postParameters.add(new BasicNameValuePair("phone_number", params[9]));
				postParameters.add(new BasicNameValuePair("google_id", params[10]));
				
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
				HttpResponse response = httpclient.execute(post);

				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
					result = new JSONObject(responseString);
				} else {
					//Closes the connection.
					System.out.println("Status: " + response.getStatusLine().getStatusCode());
					response.getEntity().getContent().close();
					return -1;
				}
			} catch (Exception e) {
				return -1;
			}

			try {
				return (Integer) result.get("id");
			} catch (JSONException e) {
				e.printStackTrace();
				return -1;
			}
		}

		protected void onPostExecute(Integer id) {
			confirm2(id);
		}
	}

	private void addReview(int id) {
		if (id == -1) {	
			Toast toast = Toast.makeText(this, "Something broke, review not sumbitted!.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
			toast.show();
			ProgressBar progress = (ProgressBar)findViewById(R.id.review_progressbar);
			progress.setVisibility(View.INVISIBLE);
		}

		AddReviewTask submit = new AddReviewTask();
		
		submit.execute("" + id,
					   "" + global.userID,
					   "" + (intent.getDoubleExtra("rating", 0)*2), 
					   intent.getStringExtra("comments"),
					   intent.getStringExtra("encodedImage"));
	}

	private void submitFinished() {
		ProgressBar progress = (ProgressBar)findViewById(R.id.review_progressbar);
		progress.setVisibility(View.INVISIBLE);

		Intent result = new Intent();
		result.putExtra("confirm", true);
		setResult(Activity.RESULT_OK, result);
		finish();
	}
}
