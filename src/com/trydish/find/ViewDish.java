package com.trydish.find;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.trydish.main.R;

public class ViewDish extends Fragment implements OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_view_dish, container, false);

		((ImageButton)(view.findViewById(R.id.flagButton))).setOnClickListener(this);
		
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
		report(button);
	}
}
