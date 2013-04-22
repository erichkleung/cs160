package com.trydish.review;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.trydish.main.R;

public class ReviewHome extends Fragment implements OnClickListener {

	private View myView;
	private ActionBar actionBar;
	private static Context context;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.activity_review_home,
				container, false);
		myView = view;
		//context = getApplicationContext();
		
		((Button)(view.findViewById(R.id.buttonDone))).setOnClickListener(this);
		
		Button b = (Button) myView.findViewById(R.id.buttonMap);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//get rid of popup window
				Intent intent = new Intent(com.trydish.find.FindHome.getContext(), MapActivity.class);
			    startActivity(intent); 
			}
		});
		return view;

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
		}
		
		Intent intent = new Intent(getActivity(), ConfirmReview.class);
		intent.putExtra("restaurant", restaurant);
		intent.putExtra("name", name);
		intent.putExtra("comments", comments);
		intent.putExtra("rating", rating);
		//TODO: Image? 
		
		startActivityForResult(intent, 1);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data.getBooleanExtra("confirm", false)) {
			Toast toast = Toast.makeText(getActivity(), "Review submitted.", Toast.LENGTH_SHORT);
			toast.show();
			
			ViewGroup viewGroup = (ViewGroup)myView.findViewById(R.id.review_form);
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				View view = viewGroup.getChildAt(i);
				if (view instanceof EditText) {
					((EditText) view).setText("");
				} else if (view instanceof RatingBar) {
					((RatingBar) view).setRating(0);
				}
			}
			FragmentManager manager = getActivity().getFragmentManager();
			manager.saveFragmentInstanceState(this);
			actionBar = getActivity().getActionBar();
			actionBar.setSelectedNavigationItem(0);
		}
		//else do nothing
	}
	
	@Override
	public void onClick(View view) {
		if (view == myView.findViewById(R.id.buttonDone)) {
			done(view);
		}
	}
	
	public void onResume(View view) {
		
	}
	
	
	

}
