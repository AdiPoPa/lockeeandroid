package com.adipopa.lockee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.droidsonroids.gif.GifTextView;

public class LoadingActivity extends AppCompatActivity {
    
    Handler handler;
    Runnable loading, maintenance;
    int LOADING_TIME = 7800;

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

        handler = new Handler();
        loading = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        };
        maintenance = new Runnable() {
            @Override
            public void run() {
                lockeeText.setVisibility(View.INVISIBLE);
                lockeeIcon.setVisibility(View.INVISIBLE);
                maintenanceText.setVisibility(View.VISIBLE);
            }
        };
    }
    
    private class onPingServer extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String ping_url = "https://lockee-cloned-andrei-b.c9users.io/android/ping/";
            try {
                URL url = new URL(ping_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(false);
                httpURLConnection.setDoInput(true);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null){
                handler.removeCallbacks(maintenance);
            } else {
                handler.removeCallbacks(loading);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(loading);
        handler.removeCallbacks(maintenance);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(loading, LOADING_TIME);
        handler.postDelayed(maintenance, LOADING_TIME);
        new onPingServer().execute();
    }
}
