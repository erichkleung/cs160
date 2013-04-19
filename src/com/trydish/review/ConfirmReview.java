package com.trydish.review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
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
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.trydish.main.R;

public class ConfirmReview extends Activity {

	Intent intent;
	ActivityResult actResult;
	
	
	//Dish and Review serve no real purpose, feel free to remove/change/replace them.
	class Dish {
		String name, restaurant;
		ArrayList<Review> reviews = new ArrayList<Review>();
		//allergies?
		
		public double getRating() {
			double ratings = 0;
			for (int i = 0; i < reviews.size(); i++) {
				ratings += reviews.get(i).rating;
			}
			ratings /= reviews.size();
			
			return ratings;
		}
	}
	
	ArrayList<Dish> DishList = new ArrayList<Dish>();
	
	class Review {
		String user;
		String comment;
		double rating;
		//picture?
		Dish dish;
	}
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_review);
		intent = getIntent();
		
		((TextView)findViewById(R.id.textViewRestaurant)).setText("Restaurant: " + intent.getStringExtra("restaurant"));
		((TextView)findViewById(R.id.textViewName)).setText("Dish Name: " + intent.getStringExtra("name"));
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
		Review currentReview = new Review();
		Dish currentDish = null;
		
		for (int i = 0; i < DishList.size(); i++) {
			if (DishList.get(i).name.equalsIgnoreCase(intent.getStringExtra("name")) && DishList.get(i).restaurant.equalsIgnoreCase(intent.getStringExtra("restaurant"))) {
				currentDish = DishList.get(i);
				break;
			}
		}
		
		if (currentDish == null) {
			currentDish = new Dish();
			currentDish.restaurant = intent.getStringExtra("restaurant");
			currentDish.name = intent.getStringExtra("name");
		}
		
		//currentReview.user = ???
		currentReview.dish = currentDish;
		currentReview.comment = intent.getStringExtra("comments");
		currentReview.rating = intent.getDoubleExtra("rating", 0);
		//allergies?
		
		ReviewTask submit = new ReviewTask();
		//submit.execute(currentDish, intent.getDoubleExtra("rating", 0), intent.getStringExtra("comments"));
		
		Intent result = new Intent();
		result.putExtra("confirm", true);
		setResult(Activity.RESULT_OK, result);
		finish();
	}
	
	
	private class ReviewTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String url = "";//"http://trydish.pythonanywhere.com/messages";

			HttpClient httpclient = new DefaultHttpClient();
			try {
				 HttpPost post = new HttpPost(url);
			     List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			     postParameters.add(new BasicNameValuePair("name", params[0]));
			     postParameters.add(new BasicNameValuePair("comment", params[1]));

			     UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
		         post.setEntity(entity);

		         httpclient.execute(post);
 	            
 	        } catch (ClientProtocolException e) {
 	        } catch (IOException e) {
 	        }

			 return null;
		}

		@Override
		protected void onPostExecute(String arg0) {
			
		}
	}
}
