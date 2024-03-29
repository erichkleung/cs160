package com.trydish.find;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
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
import android.content.Context;
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
import android.util.Base64;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trydish.main.R;
import com.trydish.review.MapActivity;

public class ViewDish extends Fragment implements OnClickListener {

	private int dishID = 35;
	private String distanceString;

	private static double latDoub;
	private static double lngDoub;
	private View myView;
	private TextView text;
	
	private String restaurant_name = "";

	Context context;

	private LruCache<String, Bitmap> mMemoryCache;
	private int screenHeight;
	private int screenWidth;
	private int imageDimension;
	private Bitmap mPlaceHolderBitmap;
	private ArrayList<String> encodedImages;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_view_dish, container, false);
		context = view.getContext();
		
		dishID = getArguments().getInt("dish_id");

		myView = view;
		((ImageButton)(view.findViewById(R.id.flagButton))).setOnClickListener(this);
		
		encodedImages = new ArrayList<String>();

		Button b = (Button) view.findViewById(R.id.mapButtonView);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//get rid of popup window
				Intent intent = new Intent(com.trydish.find.FindHome.getContext(), MapActivity.class);
				intent.putExtra("name", restaurant_name);
				startActivity(intent); 
			}
		});
		text = (TextView) view.findViewById(R.id.dish_header_text);
		text.setPadding(39, 10, 0, 10);
		text.setTextColor(Color.WHITE);
		text.setTextSize(20);
		text.setShadowLayer(1, 0, 0, Color.WHITE);
		text.setTypeface(Typeface.SANS_SERIF);

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
		
		ProgressBar progress = (ProgressBar)myView.findViewById(R.id.viewDish_progressbar);
		progress.setVisibility(View.VISIBLE);

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

	public void loadBitmap(String encoding, ImageView imageView) {
		if (cancelPotentialWork(encoding, imageView)) {
			final String imageKey = encoding;
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				final BitmapWorkerTask task = new BitmapWorkerTask(encoding, imageView);
				final AsyncDrawable asyncDrawable =
						new AsyncDrawable(this.getResources(), mPlaceHolderBitmap, task);
				imageView.setImageDrawable(asyncDrawable);
				task.execute(screenWidth, imageDimension);
			}
		}
	}

	public boolean cancelPotentialWork(String encoding, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.getData();
			if (!bitmapData.equals(encoding)) {
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
		private int width = 0;
		private int height = 0;
		private String encoding;

		public BitmapWorkerTask(String encoding, ImageView imageView) {
			// user WeakReference to ensure the ImageView can be garbage collected
			this.encoding = encoding;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background
		@Override
		protected Bitmap doInBackground(Integer... params) {
//			data = params[0];
			width = params[0];
			height = params[1];
			final Bitmap bitmap = decodeSampledBitmapFromResource(imageViewReference.get().getContext().getResources(),
					encoding, width, height);
			addBitmapToMemoryCache(encoding, bitmap);
			return bitmap;
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

		public String getData() {
			return encoding;
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

		public Bitmap decodeSampledBitmapFromResource(Resources res, String encoding, 
				int reqWidth, int reqHeight) {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inJustDecodeBounds = true;
//			BitmapFactory.decodeResource(res, resId, options);
			byte[] decodedByte = Base64.decode(encoding, 0);
		    Bitmap bm = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);
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

	private class getDishLocationTask extends AsyncTask<Integer, Void, String> {

		String toReturn;
		protected String doInBackground(Integer... dishID) {			
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
					double distLat = com.trydish.find.FindHome.getLat();
					double distLng = com.trydish.find.FindHome.getLong();
					float[] results = new float[3];
					android.location.Location.distanceBetween(distLat, distLng, latDoub, lngDoub, results);
					float distance = results[0];
					float toMiles = distance * (float)0.000621371;
					float formattedNumber = Float.parseFloat(new DecimalFormat("#.##").format(toMiles));
					toReturn = Float.toString(formattedNumber);



				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			return toReturn;
		}
		@Override
		protected void onPostExecute(String result) {
			updateDistance(result);
		}
	}


	public void updateDistance(String distance) {
		distanceString = distance;
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
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);


					String responseString = out.toString();
					out.close();
					result = new JSONObject(responseString);

				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
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
					restaurant_name = rest_name;
					dish_name = result.getString("dish_name");
					JSONObject reviewDict = result.getJSONObject("reviews");
					
					// Process and display images
					JSONArray picArray = result.getJSONArray("photos");
					String tempString;
					for(int i = 0; i < picArray.length(); i++) {
						tempString = (String) picArray.get(i);
						encodedImages.add(tempString);
					}
					// Now we call the method to actually display images
					displayImages();
					
					RatingBar rb = (RatingBar) myView.findViewById(R.id.ratingBar2);
					rb.setRating((float) (Float.parseFloat(avg_rating)/2.0));
					
					
					View currentSetBelow = (TextView) myView.findViewById(R.id.comment_text);
					RelativeLayout scroll = (RelativeLayout) myView.findViewById(R.id.RelativeLayout1);
					RelativeLayout format = (RelativeLayout) myView.findViewById(R.id.relative_format);
					
					JSONObject temp = null;
					String username = "";
					String comment = "";
					int rating = 0;
					Iterator<?> keys = reviewDict.keys();
			        while( keys.hasNext() ){
			            String key = (String)keys.next();
			            
			            if( reviewDict.get(key) instanceof JSONObject ){
			            	temp = (JSONObject) reviewDict.get(key);
			            	username = temp.getString("username");
			            	comment = temp.getString("comment");
			            	rating  = temp.getInt("rating");
			            
			            	addViews(username, comment, rating);
			            }
			        }

			} catch (Exception e) {
				System.out.println(e);
			}
			updateFields(dish_name, rest_name);
		}
	}
	
	private void displayImages() {
		LinearLayout imageHolder = (LinearLayout) myView.findViewById(R.id.image_linear);

		if (encodedImages.size() == 0) {
			ImageView imageView = new ImageView(imageHolder.getContext());
			imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(1, 0, 0, 0);
			imageView.setImageResource(R.drawable.nophoto);
			imageHolder.addView(imageView, 0);
		}
		
		for (int i = 0; i < encodedImages.size(); i++) {
			String encoding = encodedImages.get(i);
			ImageView imageView = new ImageView(imageHolder.getContext());
			imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, imageDimension)); // 255, 200
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(1, 0, 0, 0);
			loadBitmap(encoding, imageView);
			imageHolder.addView(imageView, i);
		}
		
		ProgressBar progress = (ProgressBar)myView.findViewById(R.id.viewDish_progressbar);
		progress.setVisibility(View.INVISIBLE);
	}

	public void addViews(String username, String comment, int ratingNum) {
		LinearLayout layout = (LinearLayout) myView.findViewById(R.id.Layout);

		LinearLayout newLayout = new LinearLayout(context);
		layout.addView(newLayout);

		TextView first = new TextView(context);
		first.setText(username);
		first.setTextSize(20);
		first.setTextColor(Color.BLACK);
		newLayout.addView(first);

		RatingBar rating = new RatingBar(context, null, android.R.attr.ratingBarStyleSmall);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(10, 6, 0, 0);  //left, top, right, bottom
		rating.setRating((float) (ratingNum/2.0));
		newLayout.addView(rating, layoutParams);

		TextView second = new TextView(context);
		second.setText(comment);
		second.setTextColor(Color.BLACK);
		first.setTextSize(17);
		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(0, 0, 0, 20);
		layout.addView(second, layoutParams2);
	}

	public void updateFields(String... params) {
		text.setText(Html.fromHtml("<h2>" + params[0] + "</h2>" +
				"<small>from</small>"+ " "+ params[1]+"<br />" + 
				distanceString +" <small>miles away</small>"));
	}

	


	public static double getLat() {
		return latDoub;
	}

	public static double getLong() {
		return lngDoub;
	}
}
