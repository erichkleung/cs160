package com.trydish.find;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
	
	public View getView(int position, View convertView, ViewGroup parent) {
//		ImageView imageView;
//		if (convertView == null) {  // if it's not recycled, initialize some attributes
//			imageView = new ImageView(mContext);
//			imageView.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension)); // 255, 200
//			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//			loadBitmap(mThumbIds[position], imageView);
//		} else {
//			imageView = (ImageView) convertView;
//		}
//		return imageView;
		RelativeLayout rLayout;
		if (convertView == null) {
			rLayout = new RelativeLayout(mContext);
			rLayout.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension));

			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			loadBitmap(mThumbIds[position], imageView); 

			RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
					(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			tParams.setMargins(0, 5, 0, 0);
			tParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			tParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			TextView text = new TextView(mContext); 
			text.setPadding(10, 5, 10, 5);
			text.setBackgroundColor(Color.argb(100, 0, 0, 0));
			text.setText(titles[position]); 
			text.setTextColor(Color.WHITE);                            
			text.setTypeface(Typeface.DEFAULT_BOLD);
			text.setTextSize(30);
			text.setLayoutParams(tParams);

			rLayout.addView(imageView);
			rLayout.addView(text);
		} else {
			rLayout = (RelativeLayout) convertView;
		}

		return rLayout;
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
			"SUPER AWESOME SPAGHETTI",
			"FRIED CHICKEN WINGS",
			"AMAZINGLY ASIAN DIMSUM",
			"SUPER FATTY HAMBURGER",
	};
}