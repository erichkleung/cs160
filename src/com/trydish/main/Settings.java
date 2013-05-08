package com.trydish.main;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Fragment implements OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_settings,
				container, false);

		((TextView)(view.findViewById(R.id.settings_textView_user))).setText("Hello, " + global.username + "!");
		
		((Button)(view.findViewById(R.id.settings_button_changePass))).setOnClickListener(this);
		((Button)(view.findViewById(R.id.settings_button_changeAllergies))).setOnClickListener(this);
		((Button)(view.findViewById(R.id.settings_button_logout))).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.settings_button_changePass) {
			changePass();
		} else if (view.getId() == R.id.settings_button_changeAllergies) {
			changeAllergies();
		} else if (view.getId() == R.id.settings_button_logout) {
			logout();
		}
	}
	
	public void changePass() {
		Toast toast = Toast.makeText(getActivity(), "You can't do this yet, sorry!", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
		toast.show();
	}
	
	public void changeAllergies() {
		Toast toast = Toast.makeText(getActivity(), "You can't do this yet, sorry!", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
		toast.show();
	}
	
	public void logout() {
		global.userID = -1;
		global.username = "";
		
		Toast toast = Toast.makeText(getActivity(), "You've been logged out!", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
		toast.show();
		
		Intent intent = new Intent(getActivity(), LoginHome.class);
		startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
}
