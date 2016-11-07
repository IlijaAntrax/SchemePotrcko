package com.schemetryme.potrcko.Fragents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.schemetryme.potrcko.R;

/**
 * A placeholder fragment containing a simple view.
 */
public
class BottomFragment extends Fragment
{
	View view;

	public BottomFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view =inflater.inflate(R.layout.fragment_bottom, container,false);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		//do your stuff for your fragment here

	}

}