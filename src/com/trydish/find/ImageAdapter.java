package com.trydish.find;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.trydish.main.R;

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
		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageBitmap(decodeSampledBitmapFromResource(mContext.getResources(), mThumbIds[position], imageDimension, imageDimension));
		return imageView;
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// setting raw width and raw heights
		final int rawWidth = options.outWidth;
		final int rawHeight = options.outHeight;
		int sampleSize = 1;
		if (reqWidth < rawWidth || reqHeight < reqHeight) {
	        // Calculate ratios of height and width to requested height and width
			final int widthRatio = Math.round((float) rawWidth / (float) reqWidth);
			final int heightRatio = Math.round((float) rawHeight / (float) reqHeight);
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
			sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return sampleSize;
	}
	
	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	// references to our images
	private Integer[] mThumbIds = {
			R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger
	};
}