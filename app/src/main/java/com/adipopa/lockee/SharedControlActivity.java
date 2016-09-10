package com.adipopa.lockee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import dmax.dialog.SpotsDialog;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SharedControlActivity extends AppCompatActivity {

    final Context context = this;
    GifImageView controlView;
    GifDrawable unlockDrawable, lockDrawable;
    TextView lockNicknameText, shareIDText;
    String nickname, shareID, lockStatus;
    RelativeLayout shareControlLayout;
    AlertDialog alertDialog;

    private static final String TAG_LOCK_DETAILS = "lockDetails";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_LOCK_STATUS = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_control_screen);

        shareControlLayout = (RelativeLayout) findViewById(R.id.shareControlLayout);

        LoginActivity loginActivity = new LoginActivity();
        loginActivity.finish();

        shareID = SaveSharedPreference.getSharedID(this);

        controlView = (GifImageView)findViewById(R.id.controlView);

        try {
            unlockDrawable = new GifDrawable(getResources(), R.drawable.animatedbuttonunlock);
            lockDrawable = new GifDrawable(getResources(), R.drawable.animatedbuttonlock);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lockNicknameText = (TextView) findViewById(R.id.lockNickname);
        shareIDText = (TextView) findViewById(R.id.shareID);

        shareIDText.setText(shareID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shared_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_sync: {
                new getLockDetails().execute();
                return true;
            }
            case R.id.menu_about: {
                final ViewGroup nullParent = null;
                LayoutInflater li = LayoutInflater.from(context);
                View dialogView = li.inflate(R.layout.about_dialog, nullParent);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.alertDialog);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(dialogView);


                // set dialog non cancelable
                alertDialogBuilder.setCancelable(false);

                // set dialog message
                alertDialogBuilder.setNeutralButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                // create alert dialog
                alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            }
            case R.id.menu_dismiss: {
                SaveSharedPreference.setSharedID(SharedControlActivity.this, "not shared");
                Intent i = new Intent(SharedControlActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(featureId == Window.FEATURE_ACTION_BAR && menu != null){
            if(menu.getClass().getSimpleName().equals("MenuBuilder")){
                try{
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    public void onControl(View view){
        new startControl().execute();
    }

    private class startControl extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            unlockDrawable.setSpeed(5.0f);
            lockDrawable.setSpeed(5.0f);
        }

        @Override
        protected String doInBackground(Void... params) {
            String controlLock_url = "https://lockee-andrei-b.c9users.io/android/share_mechanic/";
            // This is the login request
            try {
                URL url = new URL(controlLock_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String postData = URLEncoder.encode("shareID", "UTF-8") + "=" + URLEncoder.encode(shareID, "UTF-8");
                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
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
        protected void onPostExecute(final String result) {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    if(result != null) {
                        unlockDrawable.setSpeed(1.0f);
                        lockDrawable.setSpeed(1.0f);
                        Intent i = new Intent(SharedControlActivity.this, LoginActivity.class);
                        switch (result) {
                            case "#unlocked":
                                lockDrawable.seekTo(unlockDrawable.getCurrentPosition());
                                controlView.setImageDrawable(lockDrawable);
                                break;
                            case "#locked":
                                unlockDrawable.seekTo(lockDrawable.getCurrentPosition());
                                controlView.setImageDrawable(unlockDrawable);
                                break;
                            case "wrong code":
                                Toast.makeText(SharedControlActivity.this, "This shared lock is no longer available", Toast.LENGTH_LONG).show();
                                SaveSharedPreference.setSharedID(SharedControlActivity.this, "not shared");
                                startActivity(i);
                                finish();
                                break;
                            case "expired":
                                Toast.makeText(SharedControlActivity.this, "This shared lock has expired", Toast.LENGTH_LONG).show();
                                SaveSharedPreference.setSharedID(SharedControlActivity.this, "not shared");
                                startActivity(i);
                                finish();
                                break;
                        }
                    } else {
                        Log.e("LockControlHandler", "There was an error handling the control, please check connection");
                    }
                }
            }, 750);
        }
    }

    private class getLockDetails extends AsyncTask<Void, Void, Void> {

        ArrayList<HashMap<String, String>> lockDetails;
        String jsonStr = "";

        android.app.AlertDialog progressDialog = new SpotsDialog(SharedControlActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            shareControlLayout.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String sharePing_url = "https://lockee-andrei-b.c9users.io/android/share_details/";

            URL url;
            try {
                url = new URL(sharePing_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));

                String result = URLEncoder.encode("shareID", "UTF-8") + "=" + URLEncoder.encode(shareID, "UTF-8");

                writer.write(result);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        jsonStr += line;
                    }
                } else {
                    jsonStr = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("Response: ", "> " + jsonStr);

            lockDetails = ParseJSON(jsonStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Intent i = new Intent(SharedControlActivity.this, LoginActivity.class);
                    Toast toast;
                    TextView v;
                        switch (jsonStr) {
                            case "wrong code":
                                toast = Toast.makeText(SharedControlActivity.this, "This shared lock is no longer available", Toast.LENGTH_LONG);
                                v = (TextView) toast.getView().findViewById(android.R.id.message);
                                v.setGravity(Gravity.CENTER);
                                toast.show();
                                SaveSharedPreference.setSharedID(SharedControlActivity.this, "not shared");
                                startActivity(i);
                                finish();
                                break;
                            case "expired":
                                toast = Toast.makeText(SharedControlActivity.this, "This shared lock has expired", Toast.LENGTH_LONG);
                                v = (TextView) toast.getView().findViewById(android.R.id.message);
                                v.setGravity(Gravity.CENTER);
                                toast.show();
                                SaveSharedPreference.setSharedID(SharedControlActivity.this, "not shared");
                                startActivity(i);
                                finish();
                                break;
                            default:
                                if(lockDetails != null) {
                                    lockNicknameText.setText(nickname);
                                    shareIDText.setText(shareID);
                                    if (lockStatus.equals("#unlocked")) {
                                        controlView.setImageDrawable(lockDrawable);
                                    } else {
                                        controlView.setImageDrawable(unlockDrawable);
                                    }
                                    shareControlLayout.setVisibility(View.VISIBLE);
                                } else {
                                    Log.e("LockDetailsHandler", "There was an error getting the lock details, please check connection");
                                }
                                break;
                        }
                }
            }, 750);
        }
    }

    private ArrayList<HashMap<String, String>> ParseJSON (String json) {
        if (json != null) {
            try {
                ArrayList<HashMap<String, String>> lockInfo = new ArrayList<>();

                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                JSONArray locks = jsonObj.getJSONArray(TAG_LOCK_DETAILS);

                JSONObject c = locks.getJSONObject(0);

                nickname = c.getString(TAG_NICKNAME);
                lockStatus = c.getString(TAG_LOCK_STATUS);

                HashMap<String, String> lock = new HashMap<>();

                lock.put(TAG_NICKNAME, nickname);
                lock.put(TAG_LOCK_STATUS, lockStatus);

                lockInfo.add(lock);

                return lockInfo;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getLockDetails().execute();
    }

    // Method to require double press on the back button to close the app

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press the back button again to close the app", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
