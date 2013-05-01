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
import android.database.Cursor;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_review);
		intent = getIntent();
		
		((TextView)findViewById(R.id.textViewRestaurant)).setText("Restaurant: " + intent.getStringExtra("restaurant"));
		((TextView)findViewById(R.id.textViewName)).setText("Dish Name: " + intent.getStringExtra("name"));
		
		LinearLayout allergiesList = (LinearLayout) findViewById(R.id.Layout);
		for (int i = 0; i < global.allergy_ids.size(); i++) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(13, 13, 13, 13);
			CheckBox newAllergy = new CheckBox(this);
			
			System.out.println("start of DB code");
			
			String[] allergyID = { global.allergy_ids.get(i) };
			
			System.out.println("query DB");
			System.out.println("isOpen: " + global.allergyDB.isOpen());
			System.out.println("Path: " + global.allergyDB.getPath());
			Cursor allergyCursor = global.allergyDB.rawQuery("SELECT name FROM allergies WHERE id=?", allergyID);
			
			System.out.println("before getString");
			newAllergy.setText(allergyCursor.getString(0));
			newAllergy.setChecked(true);
			allergiesList.addView(newAllergy, params);
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
		
		if (intent.getIntExtra("dishID",-1) != -1) {
			addReview(intent.getIntExtra("dishID",-1));
		} else {
			AddDishTask addDish = new AddDishTask();
			addDish.execute(intent.getStringExtra("name"), "" + intent.getIntExtra("restaurantID", -1));
		}
	}
	
	
	private class AddReviewTask extends AsyncTask<String, Void, Boolean> {
		
		//params: dish (id), user (id), rating, comment
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
				
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
	            HttpResponse response = httpclient.execute(post);
	            
	            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                System.out.println("response: " + responseString);
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
			System.out.println("callsubmit: " + callsubmit);
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
				postParameters.add(new BasicNameValuePair("restaurant_id", params[1]));
				
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
			System.out.println("dish id: " + id);
			addReview(id);
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
		submit.execute("" + id, "" + global.userID, "" + (intent.getDoubleExtra("rating", 0)*2), intent.getStringExtra("comments"));
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
