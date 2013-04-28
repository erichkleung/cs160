package com.trydish.review;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ImageButton;

import com.trydish.main.R;

public class ReviewHome extends Fragment implements OnClickListener, OnItemClickListener {

	private View myView;
	private ActionBar actionBar;
	private static Context context;
	private int intentId = 800;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.activity_review_home,
				container, false);
		myView = view;
		//context = getApplicationContext();
		context = view.getContext();
		
		((Button)(view.findViewById(R.id.buttonDone))).setOnClickListener(this);
		((ImageButton)(view.findViewById(R.id.imageView1))).setOnClickListener(this);
		
		/*Button b = (Button) myView.findViewById(R.id.buttonMap);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//get rid of popup window
				Intent intent = new Intent(com.trydish.find.FindHome.getContext(), MapActivity.class);
			    startActivity(intent); 
			}
		});*/
		
		EditText e = (EditText) myView.findViewById(R.id.editTextRestaurant);
		e.addTextChangedListener(new TextWatcher() {
		
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
		
		//for autocomplete google API
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.editTextRestaurant);
	    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(context, R.layout.list_item));
	    autoCompView.setOnItemClickListener(this);
		
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
		intent.putExtra("dishID", -1);
		intent.putExtra("restaurantID", 1);
		//TODO: Image?
		
		startActivityForResult(intent, 1);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//added call to super for compatibility 
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
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
			} else if (requestCode == intentId) {
				//Using http://www.pocketmagic.net/2011/02/android-photopicker-using-intents-and-gallery/#.UXyyYCugkSQ as a model
				if (data != null) {
					Log.d("trydish", "idButSelPic Photopicker: " + data.getDataString());
					Cursor cursor = getContext().getContentResolver().query(data.getData(), null, null, null, null);
					cursor.moveToFirst();  //if not doing this, 01-22 19:17:04.564: ERROR/AndroidRuntime(26264): Caused by: android.database.CursorIndexOutOfBoundsException: Index -1 requested, with a size of 1
					int idx = cursor.getColumnIndex(ImageColumns.DATA);
					String fileSrc = cursor.getString(idx); 
					Log.d("trydish", "Picture:" + fileSrc);

					Bitmap bitmapPreview = BitmapFactory.decodeFile(fileSrc); //load preview image
					BitmapDrawable bmpDrawable = new BitmapDrawable(Resources.getSystem(), bitmapPreview);
					((ImageButton)(myView.findViewById(R.id.imageView1))).setImageDrawable(bmpDrawable);
				}
				else {
					Log.d("trydish", "idButSelPic Photopicker canceled");
				}
			}
			//else do nothing
		}

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
		// TODO Auto-generated method stub
		String str = (String) adapterView.getItemAtPosition(position);
		//String refToQuery = (String) PlacesAutoCompleteAdapter.getRef(position);
		String refToQuery = (String) ((PlacesAutoCompleteAdapter)adapterView.getAdapter()).getRef(position);
		System.out.println("the ref clicked was: "+ str);
        Toast.makeText(context, refToQuery, Toast.LENGTH_LONG).show();
	}
	
	public void addImage(View v) {
		//Log.d("yo", "picture clicked");
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, intentId);
	}
	
	
	

}
