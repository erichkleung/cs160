package com.trydish.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginHome extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_login_home);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
	}
	
	public void loginCheck(View view) {
    	Intent intent = new Intent(this, PostLoginHome.class);
    	startActivity(intent);
    	overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
	}
	
	public void signupButton(View view) {
		Intent intent = new Intent(this, SignupHome.class);
		startActivity(intent);
		overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
	}

}
