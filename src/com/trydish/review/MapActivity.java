package com.trydish.review;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.trydish.main.R;

public class MapActivity extends Activity {

	GoogleMap googleMap;
	private double latitude;
	private double longitude;
	
	private String restaurant_name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		latitude = com.trydish.find.ViewDish.getLat();
		longitude = com.trydish.find.ViewDish.getLong();
		
		Intent intent = getIntent();
		restaurant_name  = intent.getStringExtra("name");
		
		//following code based off of http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-using-api-v2-with-supportmapfragment/
		// and http://stackoverflow.com/questions/14074129/google-maps-v2-set-both-my-location-and-zoom-in
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();

		}else { // Google Play Services are available

			// Getting reference to the SupportMapFragment of activity_main.xml
			MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

			// Getting GoogleMap object from the fragment
			googleMap = fm.getMap();
			
			CameraUpdate center=
			        CameraUpdateFactory.newLatLng(new LatLng(latitude,
			                                                 longitude));
			    CameraUpdate zoom=CameraUpdateFactory.zoomTo(18);

			    googleMap.moveCamera(center);
			    googleMap.animateCamera(zoom);
			    //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			    
			    googleMap.addMarker(new MarkerOptions()
			            .position(new LatLng(latitude, longitude))
			            .title(restaurant_name));
			    }

	}

	public MapActivity() {

	}

	
}