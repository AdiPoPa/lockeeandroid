package com.adipopa.lockee;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import pl.droidsonroids.gif.GifTextView;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove the action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        this.setContentView(R.layout.loading_screen);
        
        final GifTextView lockeeText = (GifTextView) findViewById(R.id.lockeeText);
        final GifTextView lockeeIcon = (GifTextView) findViewById(R.id.lockeeIcon);
        final TextView maintenanceText = (TextView) findViewById(R.id.maintenanceText);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent i = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 7800);
    }
}
