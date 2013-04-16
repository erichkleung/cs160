package com.trydish.find;
import android.content.Context;
import android.graphics.Bitmap;
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

	public ImageAdapter(Context c) {
		mContext = c;
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
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(390, 390)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		} else {
			imageView = (ImageView) convertView;
		}
		Drawable draw = mContext.getResources().getDrawable(mThumbIds[position]);
		Bitmap d = ((BitmapDrawable)draw).getBitmap();
		Bitmap newBitmap = Bitmap.createScaledBitmap(d, (int)(d.getWidth() * 1.5), (int)(d.getHeight() * 1.5), false);
		imageView.setImageBitmap(newBitmap);
		return imageView;
	}

	// references to our images
	private Integer[] mThumbIds = {
			R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,

	};
}