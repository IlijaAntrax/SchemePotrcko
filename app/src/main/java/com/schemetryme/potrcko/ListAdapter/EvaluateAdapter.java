package com.schemetryme.potrcko.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.schemetryme.potrcko.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefan on 12/24/2016.
 */

public class EvaluateAdapter extends ArrayAdapter<HashMap<String, String>> {

    public EvaluateAdapter(Context ctx, List<HashMap<String, String>> list){
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
        TextView name = (TextView) convertView.findViewById(R.id.item_evaluate_list_name);
        TextView text = (TextView) convertView.findViewById(R.id.item_evaluate_list_text);
        // Populate the data into the template view using the data object
        name.setText(route.get("name"));
        text.setText(route.get("text"));

        // Return the completed view to render on screen
        return convertView;
    }

}
