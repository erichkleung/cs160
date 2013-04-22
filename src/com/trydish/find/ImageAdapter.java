package com.trydish.find;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.trydish.main.R;
import com.trydish.main.Workers.AsyncDrawable;
import com.trydish.main.Workers.BitmapWorkerTask;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private int screenWidth;
	private int screenHeight;
	private int imageDimension;
	private Bitmap mPlaceHolderBitmap;

	public ImageAdapter(Context c) {
		mContext = c;
		screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		imageDimension = (int) Math.round(screenWidth / 1.5 - 10);
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		mPlaceHolderBitmap = Bitmap.createBitmap(screenWidth, imageDimension, conf);
	}

	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	// snippet from :
	// http://stackoverflow.com/questions/7706913/overlay-text-over-imageview-in-framelayout-progrmatically-android
	
	@SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout rLayout;
		if (convertView == null) {
			// Setting up the parent RelativeLayout
			rLayout = new RelativeLayout(mContext);
			rLayout.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension));
			
			// Setting up our ImageView
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			loadBitmap(mThumbIds[position], imageView);
			
			// Creating our different TextViews
			TextView foodText = foodNameText(titles[position], restaurants[position]);
			// TextView restaurant = restaurantText(restaurants[position]);
			
			// Hardcoding rating bar
			RatingBar ratingBar = new RatingBar(mContext, null, android.R.attr.ratingBarStyleIndicator);
			RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
					(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tParams.setMargins(0, 55, 0, 0);
			tParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			tParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			ratingBar.setLayoutParams(tParams);
			ratingBar.setRating((float)3.5);
			ratingBar.setBackgroundColor(Color.argb(100, 0, 0, 0));
			ratingBar.setPadding(0, 0, 0, 5);
			
			// Adding everything to the RelativeLayout
			rLayout.addView(imageView);
			rLayout.addView(foodText);
			// rLayout.addView(restaurant);
			rLayout.addView(ratingBar);
			rLayout.addView(distanceText(distances[position]));
		} else {
			rLayout = (RelativeLayout) convertView;
		}

		return rLayout;
	}
	
	@SuppressLint("NewApi")
	public TextView distanceText(String location) {
		// Setting the RelativeLayout parameters for the Food Name TextView
		TextView text = new TextView(mContext);
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tParams.setMargins(0, 0, 0, 5);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		text.setLayoutParams(tParams);
		// These are TextView specific parameters (we're done messing with RelativeLayout here)
		text.setPadding(10, 5, 10, 5);
		text.setBackgroundColor(Color.argb(100, 0, 0, 0));
		text.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
		text.setText(Html.fromHtml(location + " <i><small>miles away</small></i>"));
		text.setTextColor(Color.WHITE);                            
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setTextSize(20);
		
		return text;
	}
	
	@SuppressLint("NewApi")
	public TextView restaurantText(String restaurant) {
		// Setting the RelativeLayout parameters for the Food Name TextView
		TextView text = new TextView(mContext);
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tParams.setMargins(0, 0, 0, 52);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		text.setLayoutParams(tParams);
		// These are TextView specific parameters (we're done messing with RelativeLayout here)
		text.setPadding(10, 5, 10, 5);
		text.setBackgroundColor(Color.argb(100, 0, 0, 0));
		text.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
		text.setText(Html.fromHtml("<i><small>from</small></i> " + restaurant));
		text.setTextColor(Color.WHITE);                            
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setTextSize(20);
		
		return text;
	}
	
	@SuppressLint("NewApi")
	public TextView foodNameText(String foodName, String restaurant) {
		// Setting the RelativeLayout parameters for the Food Name TextView
		TextView text = new TextView(mContext);
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, 50);
		tParams.setMargins(0, 5, 0, 0);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		tParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		text.setLayoutParams(tParams);
		// These are TextView specific parameters (we're done messing with RelativeLayout here)
		text.setPadding(10, 0, 10, 5);
		text.setBackgroundColor(Color.argb(100, 0, 0, 0));
		text.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
		text.setText(Html.fromHtml(foodName + 
				" <i><small><small>from</small></small></i> " + 
				"<small>" + restaurant + "</small>"));
		text.setTextColor(Color.WHITE);                            
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setTextSize(25);
		
		return text;
	}
	
	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable =
					new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId, screenWidth, imageDimension);
		}
	}
	
	public boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);
		
		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.getData();
			if (bitmapData != data) {
				// cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// the same work already in progress
				return false;
			}
		}
		
		// no task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	// references to our images
	private Integer[] mThumbIds = {
			R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
	};
	
	private String[] titles = {
			"Super Awesome Spaghetti",
			"Fried Chicken Wings",
			"Amazingly Asian Dimsum",
			"Super Fatty Hamburger",
			"Super Awesome Spaghetti",
			"Fried Chicken Wings",
			"Amazingly Asian Dimsum",
			"Super Fatty Hamburger",
	};
	
	private String[] restaurants = {
			"Spaghetti Me",
			"Wing Go",
			"The Chinese",
			"Heartattack Grill",
			"Spaghetti Me",
			"Wing Go",
			"The Chinese",
			"Heartattack Grill",
	};
	
	private String[] distances = {
			"0.4",
			"0.6",
			"1.2",
			"1.5",
			"0.4",
			"0.6",
			"1.2",
			"1.5",
	};
}