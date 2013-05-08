package com.trydish.find;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trydish.main.R;
import com.trydish.review.MapActivity;

public class ViewDish extends Fragment implements OnClickListener {

	private int dishID = 5;
	
	private static double latDoub;
	private static double lngDoub;
	private View myView;
	private TextView text;
	private int numReviews = 0;

	private LruCache<String, Bitmap> mMemoryCache;
	private int screenHeight;
	private int screenWidth;
	private int imageDimension;
	private Bitmap mPlaceHolderBitmap;
	private Integer[] mThumbIds = {
			R.drawable.wings, R.drawable.wings2, R.drawable.wings3,
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_view_dish, container, false);
		
		myView = view;
		((ImageButton)(view.findViewById(R.id.flagButton))).setOnClickListener(this);

		Button b = (Button) view.findViewById(R.id.mapButtonView);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//get rid of popup window
				Intent intent = new Intent(com.trydish.find.FindHome.getContext(), MapActivity.class);
				startActivity(intent); 
			}
		});
		text = (TextView) view.findViewById(R.id.dish_header_text);
		text.setPadding(39, 10, 0, 10);
		text.setTextColor(Color.WHITE);
		text.setTextSize(20);
		text.setShadowLayer(1, 0, 0, Color.WHITE);
		text.setTypeface(Typeface.SANS_SERIF);
		text.setText(Html.fromHtml("<h2>Buffalo Wings</h2>" +
				"<small>from</small> Buffalo Wild Wings<br />" + 
			"10.5 <small>miles away</small>"));

		screenHeight = getResources().getDisplayMetrics().heightPixels;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		imageDimension = (int) Math.round(screenWidth / 1.5 - 10);
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		mPlaceHolderBitmap = Bitmap.createBitmap(screenWidth, imageDimension, conf);

		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		LinearLayout imageHolder = (LinearLayout) view.findViewById(R.id.image_linear);

		for (int i = 0; i < mThumbIds.length; i++) {
			ImageView imageView = new ImageView(imageHolder.getContext());
			imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(1, 0, 0, 0);
			loadBitmap(mThumbIds[i], imageView);
			imageHolder.addView(imageView, i);
		}

		getDishLocationTask dl = new getDishLocationTask();
		dl.execute(dishID);
		
		getDishInformationTask di = new getDishInformationTask();
		di.execute(dishID);

		return view;

	}

	//this goes to the "report" button that doesn't actually do anything
	public void report(View button) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Reason for Report:");
		builder.setTitle("Report Dish");

		final EditText input = new EditText(getActivity());
		input.setId(0);
		builder.setView(input);

		builder.setPositiveButton("Send Report", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast toast = Toast.makeText(getActivity(), "Report sent.", Toast.LENGTH_SHORT);
				toast.show();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick(View button) {
		if (button.getId() == R.id.flagButton) {
			report(button);
		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			final String imageKey = String.valueOf(resId);
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
				final AsyncDrawable asyncDrawable =
						new AsyncDrawable(this.getResources(), mPlaceHolderBitmap, task);
				imageView.setImageDrawable(asyncDrawable);
				task.execute(resId, screenWidth, imageDimension);
			}
		}
	}

	public boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

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

	public BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
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
			final Bitmap bitmap = decodeSampledBitmapFromResource(imageViewReference.get().getContext().getResources(),
					data, width, height);
			addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			//			if (isCancelled()) {
			//				bitmap = null;
			//			}
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				//				if (imageView != null) {
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
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

	class AsyncDrawable extends BitmapDrawable {
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

	private class getDishLocationTask extends AsyncTask<Integer, Void, Void> {

		protected Void doInBackground(Integer... dishID) {			
			String url = "http://trydish.pythonanywhere.com/get_location/" + dishID[0];

			HttpResponse response;
			HttpClient httpclient = new DefaultHttpClient();

			try {
				response = httpclient.execute(new HttpGet(url));
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);

					String responseString = out.toString();
					out.close(); 
					JSONObject result = new JSONObject(responseString);

					JSONArray jArray = result.getJSONArray("id");
					JSONObject latAndLong = jArray.getJSONObject(0);
					String lat = latAndLong.getString("lat");
					String lng = latAndLong.getString("long");
					latDoub = Double.parseDouble(lat);
					lngDoub = Double.parseDouble(lng);



				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return null;
		}
	}
	
	
	private class getDishInformationTask extends AsyncTask<Integer, Void, JSONObject> {

		String rest_name;
		JSONObject result;
		String avg_rating;
		String lng;
		String lat;
		String dish_name;
		protected JSONObject doInBackground(Integer... dishID) {			
			String url = "http://trydish.pythonanywhere.com/get_dish_info/" + dishID[0];

			HttpResponse response;
			HttpClient httpclient = new DefaultHttpClient();

			try {
				response = httpclient.execute(new HttpGet(url));
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					System.out.println("response was ok");
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);

					
					String responseString = out.toString();
					out.close();
					System.out.println(responseString);
					result = new JSONObject(responseString);
					System.out.println("object is " + result);

					//JSONArray jArray = result.getJSONArray("id");
					//JSONObject latAndLong = jArray.getJSONObject(0);
					
					/*
					avg_rating = result.getString("avg_rating");
					lng = result.getString("long");
					lat = result.getString("lat");
					rest_name = result.getString("rest_name");
					dish_name = result.getString("dish_name");
					JSONObject reviewDict = result.getJSONObject("reviews");
					
					System.out.println(avg_rating);
					System.out.println(lat);
					System.out.println(lng);
					System.out.println(rest_name);
					System.out.println(dish_name);
					//System.out.println(reviewDict);
					
					
					
					
					View currentSetBelow = (TextView) myView.findViewById(R.id.comment_text);
					Iterator<?> keys = reviewDict.keys();
			        while( keys.hasNext() ){
			            String key = (String)keys.next();
			            if( reviewDict.get(key) instanceof JSONObject ){
			            	numReviews++;
			            	System.out.println(reviewDict.get(key));
			            	//RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
			            }
			        }
					

					 */

				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					System.out.println("connection closed");
					return null;
				}
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
			
			return result;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				
					avg_rating = result.getString("avg_rating");
					lng = result.getString("long");
					lat = result.getString("lat");
					rest_name = result.getString("rest_name");
					dish_name = result.getString("dish_name");
					JSONObject reviewDict = result.getJSONObject("reviews");
					
					System.out.println(avg_rating);
					System.out.println(lat);
					System.out.println(lng);
					System.out.println(rest_name);
					System.out.println(dish_name);
					//System.out.println(reviewDict);
					
					RatingBar rb = (RatingBar) myView.findViewById(R.id.ratingBar2);
					rb.setRating(Float.parseFloat(avg_rating));
					
					
					View currentSetBelow = (TextView) myView.findViewById(R.id.comment_text);
					RelativeLayout scroll = (RelativeLayout) myView.findViewById(R.id.RelativeLayout1);
					RelativeLayout format = (RelativeLayout) myView.findViewById(R.id.relative_format);
					Iterator<?> keys = reviewDict.keys();
			        while( keys.hasNext() ){
			            String key = (String)keys.next();
			            if( reviewDict.get(key) instanceof JSONObject ){
			            	numReviews++;
			            	System.out.println(reviewDict.get(key));
			            	
			            	addViews(currentSetBelow, scroll, format);
			            	//RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
			            }
			        }

				
			} catch (Exception e) {
				System.out.println(e);
			}
			updateFields(dish_name, rest_name);
		}
	}
	
	public void addViews(View currentSetBelow, RelativeLayout scroll, RelativeLayout format) {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(format.getLayoutParams());
    	lp.addRule(RelativeLayout.BELOW, currentSetBelow.getId());
    	RelativeLayout layout = new RelativeLayout(com.trydish.find.FindHome.getContext());
    	layout.setLayoutParams(lp);
    	
    	TextView first = new TextView(com.trydish.find.FindHome.getContext());
        TextView second = new TextView(com.trydish.find.FindHome.getContext());

        first.setText("First");
        first.setId(1);

        second.setText("Second");
        second.setId(2);

        RelativeLayout.LayoutParams lpSecond = new RelativeLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout.addView(second, lpSecond);

        RelativeLayout.LayoutParams lpFirst = new RelativeLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpFirst.addRule(RelativeLayout.RIGHT_OF, second.getId());
        layout.addView(first, lpFirst);
    	scroll.addView(layout, lp);
	}
	
	public void updateFields(String... params) {
		System.out.println("getting here?" + " rest name is : " + params[0]);
		//text = (TextView) myView.findViewById(R.id.dish_header_text);
		System.out.println("part of  the way" + text);
		text.setText(Html.fromHtml("<h2>" + params[0] + "</h2>" +
				"<small>from</small>"+ " "+ params[1]+"<br />" + 
				"10.5 <small>miles away</small>"));
		//text.setText(rest_name);
		//text.postInvalidate();
		System.out.println("all the way");
	}

	public static double getLat() {
		return latDoub;
	}

	public static double getLong() {
		return lngDoub;
	}
}
