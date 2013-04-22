package com.trydish.review;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trydish.main.PostLoginHome;
import com.trydish.main.R;

public class ConfirmReview extends Activity {

	Intent intent;
	ActivityResult actResult;
	String restaurant, dish;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_review);
		intent = getIntent();
		
		restaurant = intent.getStringExtra("restaurant");
		dish = intent.getStringExtra("name");
		((TextView)findViewById(R.id.textViewRestaurant)).setText("Restaurant: " + restaurant);
		((TextView)findViewById(R.id.textViewName)).setText("Dish Name: " + dish);
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
		
		AddReviewTask submit = new AddReviewTask();
		//submit.execute(dish, restaurant, "NAME GOES HERE", "" + intent.getDoubleExtra("rating", 0), intent.getStringExtra("comments"));
		
		Intent result = new Intent();
		result.putExtra("confirm", true);
		setResult(Activity.RESULT_OK, result);
		finish();
	}
	
	
	private class AddReviewTask extends AsyncTask<String, Void, String> {

		
		//params: dish, restaurant, user, rating, comments
		@Override
		protected String doInBackground(String... params) {
			String url = "http://trydish.pythonanywhere.com/login";
			String responseString;

			HttpClient httpclient = new DefaultHttpClient();
			
			HttpPost post = new HttpPost(url);
			try {
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("dish", params[0]));
				postParameters.add(new BasicNameValuePair("restaurant", params[1]));
				postParameters.add(new BasicNameValuePair("user", params[2]));
				postParameters.add(new BasicNameValuePair("rating", params[3]));
				postParameters.add(new BasicNameValuePair("comment", params[4]));
				
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
	            HttpResponse response = httpclient.execute(post);
	            
	            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();

	            } else {
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                return "false";
	            }
	        } catch (ClientProtocolException e) {
	        	return "false";
	        } catch (IOException e) {
	        	return "false";
	        }
			if (responseString.indexOf("true") != -1) {
				return "true";
			} else {
				return "false";
			}
    	}
    
		@Override
		protected void onPostExecute(String login) {
			checkLogin(login);
		}
    	
    }
	
	private void checkLogin(String login) {
		if (login.equalsIgnoreCase("true")) {
			Toast toast = Toast.makeText(this, "Thank you for logging in!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
			toast.show();
			
			Intent intent = new Intent(this, PostLoginHome.class);
	    	startActivity(intent);
	    	overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
		} else {
			Toast toast = Toast.makeText(this, "Username or password is incorrect.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
			toast.show();
			
			EditText password = (EditText)findViewById(R.id.login_password);
			password.setText("");
		}
	}
}
