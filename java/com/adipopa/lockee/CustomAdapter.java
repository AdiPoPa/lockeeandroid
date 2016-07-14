package com.adipopa.lockee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<Data> {

    public CustomAdapter(Context context, Data[] datas) {
        super(context, R.layout.main_list, datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Data data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_list, parent, false);
        }
        // Lookup view for data population

        // Populate the data into the template view using the data object

        TextView cityText = (TextView) convertView.findViewById(R.id.cityText);
        TextView addressText = (TextView) convertView.findViewById(R.id.addressText);

        cityText.setText(data.city);
        addressText.setText(data.address);

        // Return the completed view to render on screen
        return convertView;
    }
}
