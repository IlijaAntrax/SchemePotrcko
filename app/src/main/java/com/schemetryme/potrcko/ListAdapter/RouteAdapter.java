package com.schemetryme.potrcko.ListAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefan on 11/8/2016.
 */

public class RouteAdapter extends ArrayAdapter<HashMap<String, String>> {

    public RouteAdapter(Context ctx, List<HashMap<String, String>> list){
        super(ctx,0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        HashMap<String, String> route = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }
        // Lookup view for data population
        TextView dustance = (TextView) convertView.findViewById(R.id.route_distance);
        TextView duration = (TextView) convertView.findViewById(R.id.route_duration);
        // Populate the data into the template view using the data object
        dustance.setText(route.get("distance"));
        duration.setText(route.get("duration"));

        if(position == 0){
            convertView.setSelected(true);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
