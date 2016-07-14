package com.adipopa.lockee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Data data1 = new Data("Turda", "Str. Lotus 19");
        Data data2 = new Data("Cluj-Napoca", "Str. Meteor 19");
        Data data3 = new Data("Bucuresti", "Str. Basarabiei");

        Data[] dataArray = {data1, data2, data3};

        ListAdapter CustomAdapter = new CustomAdapter(this, dataArray);
        ListView myListView = (ListView) findViewById(R.id.mainList);

        myListView.setAdapter(CustomAdapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        ImageView listIcon = (ImageView) view.findViewById(R.id.listIcon);
                        listIcon.setImageResource(R.mipmap.lockicon);
                    }
                }
        );
    }

}
