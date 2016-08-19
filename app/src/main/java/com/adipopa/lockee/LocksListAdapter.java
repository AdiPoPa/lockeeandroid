package com.adipopa.lockee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocksListAdapter extends ArrayAdapter<Data> {

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
        
        TextView nickname = (TextView) convertView.findViewById(R.id.nicknameText);
        TextView lockID = (TextView) convertView.findViewById(R.id.lockIDText);
        TextView shareID = (TextView) convertView.findViewById(R.id.shareIDText);
        TextView is_open = (TextView) convertView.findViewById(R.id.statusText);

        nickname.setText(data.nickname);
        lockID.setText(data.lockID);
        shareID.setText(data.shareID);
        is_open.setText(data.is_open);

        // Return the completed view to render on screen
        return convertView;
    }
}
