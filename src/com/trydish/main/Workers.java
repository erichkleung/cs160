package com.trydish.main;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class Workers {
	
	public static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int data = 0;
		private int width = 0;
		private int height = 0;
		
		public BitmapWorkerTask(ImageView imageView) {
			// user WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		// Decode image in background
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			width = params[1];
			height = params[2];
			return decodeSampledBitmapFromResource(imageViewReference.get().getContext().getResources(),
					data, width, height);
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
		
		public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
			if (imageView != null) {
				final Drawable drawable = imageView.getDrawable();
				if (drawable instanceof AsyncDrawable) {
					final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
					return asyncDrawable.getBitmapWorkerTask();
				}
			}
			return null;
		}
		
		public int getData() {
			return data;
		}
		
		public int calculateInSampleSize(BitmapFactory.Options options, 
				int reqWidth, int reqHeight) {
			
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
		
		public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, 
				int reqWidth, int reqHeight) {

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
	}
	
	public static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
		
		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference =
					new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}
		
		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

}
