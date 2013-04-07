package com.trydish.review;

import java.util.ArrayList;

import android.app.Fragment;
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
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_review_home,
				container, false);
		myView = view;
		
		((Button)(view.findViewById(R.id.buttonDone))).setOnClickListener(this);
		
		
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
		
		getActivity().startActivityForResult(intent, 1);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("Activity result");
		if (data.getBooleanExtra("confirm", false)) {
			Toast toast = Toast.makeText(getActivity(), "Review submitted.", Toast.LENGTH_SHORT);
			toast.show();
			
			//TODO: Go back to find?
		}
		//else do nothing
	}
	
	@Override
	public void onClick(View view) {
		if (view == myView.findViewById(R.id.buttonDone)) {
			done(view);
		}
	}

}
