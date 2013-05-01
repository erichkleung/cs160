package com.trydish.main;

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
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SignupAllergies extends Activity {
	
	private int leftRight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup_allergies);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.allergies_warning)
			   .setTitle(R.string.important_allergies);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	// used to add allergies on-the-fly
	public void addAllergy(View view) {
    	EditText editText = (EditText) findViewById(R.id.add_new_allergy_box);
    	String message = editText.getText().toString();
    	
    	if (message.equals("")) {
    		return;
    	}
    	
    	LinearLayout allergiesList;
    	// leftRight is used to switch between the two LinearLayouts
    	// holding the checkboxes
    	if (leftRight == 0) {
    		allergiesList = (LinearLayout) findViewById(R.id.left_check_boxes);
    		leftRight++;
    	} else {
    		allergiesList = (LinearLayout) findViewById(R.id.right_check_boxes);
    		leftRight--;
    	}
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.setMargins(13, 13, 13, 13);
    	CheckBox newAllergy = new CheckBox(this);
    	newAllergy.setText(message);
    	newAllergy.setChecked(true);
    	newAllergy.setTextColor(Color.BLACK);
    	newAllergy.setTextSize(30);
    	allergiesList.addView(newAllergy, params);
    	
    	editText.setText("");
	}
	
	public void signupNext(View view) {
		LinearLayout allergiesList = (LinearLayout) findViewById(R.id.left_check_boxes);
		int childcount = allergiesList.getChildCount();
		ArrayList<String> allergies = new ArrayList<String>();
		for (int i=0; i < childcount; i++){
		      CheckBox v = (CheckBox)allergiesList.getChildAt(i);
		      if (v.isChecked()) {
		    	  allergies.add(v.getText().toString().toLowerCase());
		      }
		}
		
		allergiesList = (LinearLayout) findViewById(R.id.right_check_boxes);
		childcount = allergiesList.getChildCount();
		for (int i=0; i < childcount; i++) {
		      CheckBox v = (CheckBox)allergiesList.getChildAt(i);
		      if (v.isChecked()) {
		    	  allergies.add(v.getText().toString().toLowerCase());
		      }
		}
		
		AddAllergiesTask a = new AddAllergiesTask();
		a.execute(allergies);
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
	}

	
	private class AddAllergiesTask extends AsyncTask<ArrayList<String>, Void, Void> {

		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			String url = "http://trydish.pythonanywhere.com/add_user_allergies/" + global.userID;
			String responseString;
			JSONObject result;

			HttpClient httpclient = new DefaultHttpClient();
			
			HttpPost post = new HttpPost(url);
			try {
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				for (int i = 0; i < params[0].size(); i++) {
					postParameters.add(new BasicNameValuePair("allergy", params[0].get(i)));
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
				post.setEntity(entity);
	            HttpResponse response = httpclient.execute(post);
	            
	            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                result = new JSONObject(responseString);
	                
	                ArrayList<String> list = new ArrayList<String>();
	                JSONArray jArray = result.getJSONArray("allergy_ids");
	                for(int i = 0 ; i < jArray.length() ; i++) {
	                    list.add(jArray.getString(i));
	                }
	                global.allergy_ids = list;
	                
	            } else {
	                //Closes the connection.
	            	System.out.println("Status: " + response.getStatusLine().getStatusCode());
	                response.getEntity().getContent().close();
	                return null;
	            }
	        } catch (Exception e) {
	        	return null;
	        }
			nextScreen();
			return null;
    	}
    }
	
	private void nextScreen() {
		Intent intent = new Intent(this, PostLoginHome.class);
		startActivity(intent);
		overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
	}

}
