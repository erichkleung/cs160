package com.trydish.find;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.trydish.main.R;
import com.trydish.main.Workers.BitmapWorkerTask;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private int screenWidth;
	private int screenHeight;

	public ImageAdapter(Context c) {
		mContext = c;
		screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		int imageDimension = screenWidth / 2 - 10;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(imageDimension, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(mThumbIds[position], imageDimension, imageDimension);
		} else {
			imageView = (ImageView) convertView;
		}
		return imageView;
	}	

	// references to our images
	private Integer[] mThumbIds = {
			R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
	};
}