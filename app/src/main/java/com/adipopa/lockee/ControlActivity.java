/****************************************************************************************
 *                                                                                       *
 *   Copyright (C) 2016 Glimpse Team                                                     *
 *                                                                                       *
 *       This file is part of the Lockee project and is hereby protected by copyright    *
 *   and can not be copied and/or distributed without the express permission of all      *
 *   the Glimpse Team members.                                                           *
 *                                                                                       *
 ****************************************************************************************/

package com.adipopa.lockee;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import dmax.dialog.SpotsDialog;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ControlActivity extends AppCompatActivity {

    final Context context = this;
    AlertDialog alertDialog;
    GifImageView controlView;
    GifDrawable unlockDrawable, lockDrawable;
    String email, nickname, lockInnerID, lockStatus, staticShareID, sessionShareID, expirationDate;
    TextView shareIDText, shareSession, sessionShareIDText, expirationDateText, lockNicknameText, lockIDText, expirationDateHolder;
    RelativeLayout controlLayout;
    LinearLayout sessionDialog;
    Boolean inProgress = false;

    private static final String TAG_LOCK_DETAILS = "lockDetails";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_LOCK_STATUS = "status";
    private static final String TAG_STATIC_SHARE_ID = "staticShareID";
    private static final String TAG_SESSION_SHARE_INFO = "sessionShareInfo";
    private static final String TAG_SESSION_SHARE_ID = "sessionShareID";
    private static final String TAG_EXPIRATION_DATE = "sessionExpire";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = SaveSharedPreference.getLoginStatus(this);

        controlLayout = (RelativeLayout) findViewById(R.id.controlLayout);

        controlView = (GifImageView)findViewById(R.id.controlView);

        try {
            unlockDrawable = new GifDrawable(getResources(), R.drawable.animated_button_unlock_white);
            lockDrawable = new GifDrawable(getResources(), R.drawable.animated_button_lock_white);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lockNicknameText = (TextView)findViewById(R.id.lockNickname);
        lockIDText = (TextView)findViewById(R.id.lockID);
        shareIDText = (TextView)findViewById(R.id.shareID);
        shareSession = (TextView) findViewById(R.id.sessionShare);

        Bundle itemData = getIntent().getExtras();
        if(itemData == null){
            return;
        }
        lockInnerID = itemData.getString("lockID");
        lockIDText.setText(lockInnerID);

        shareSession.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        shareSession.setTextColor(Color.parseColor("#999999"));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shareSession.setTextColor(Color.parseColor("#454545"));
                                final ViewGroup nullParent = null;
                                LayoutInflater li = LayoutInflater.from(context);
                                View dialogView = li.inflate(R.layout.session_dialog, nullParent);

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.alertDialog);

                                final NumberPicker dayPicker = (NumberPicker) dialogView.findViewById(R.id.dayPicker);
                                final NumberPicker hourPicker = (NumberPicker) dialogView.findViewById(R.id.hourPicker);
                                final NumberPicker minutePicker = (NumberPicker) dialogView.findViewById(R.id.minutePicker);

                                expirationDateHolder = (TextView) dialogView.findViewById(R.id.expirationDateHolder);
                                sessionShareIDText = (TextView) dialogView.findViewById(R.id.sessionShareIDText);
                                expirationDateText = (TextView) dialogView.findViewById(R.id.expirationDateText);

                                sessionDialog = (LinearLayout) dialogView.findViewById(R.id.sessionDialog);

                                String[] days = new String[100];
                                for(int i=0; i<days.length; i++)
                                    days[i] = Integer.toString(i);
                                dayPicker.setDisplayedValues(days);
                                dayPicker.setMinValue(0);
                                dayPicker.setMaxValue(99);
                                dayPicker.setValue(0);
                                dayPicker.setWrapSelectorWheel(false);

                                String[] hours = new String[24];
                                for(int i=0; i<hours.length; i++)
                                    hours[i] = Integer.toString(i);
                                hourPicker.setDisplayedValues(hours);
                                hourPicker.setMinValue(0);
                                hourPicker.setMaxValue(23);
                                hourPicker.setValue(0);
                                hourPicker.setWrapSelectorWheel(false);

                                String[] mins = new String[60];
                                for(int i=0; i<mins.length; i++)
                                    mins[i] = Integer.toString(i);
                                minutePicker.setDisplayedValues(mins);
                                minutePicker.setMinValue(0);
                                minutePicker.setMaxValue(59);
                                minutePicker.setValue(0);
                                minutePicker.setWrapSelectorWheel(false);

                                // set prompts.xml to alertdialog builder
                                alertDialogBuilder.setView(dialogView);

                                // set dialog message
                                alertDialogBuilder.setPositiveButton("Generate", null);

                                // create alert dialog
                                alertDialog = alertDialogBuilder.create();

                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                        b.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                int pickedValue = dayPicker.getValue();
                                                String days = Integer.toString(pickedValue);
                                                pickedValue = hourPicker.getValue();
                                                String hours = Integer.toString(pickedValue);
                                                pickedValue = minutePicker.getValue();
                                                String minutes = Integer.toString(pickedValue);
                                                new onSessionShareID().execute("generate", days, hours, minutes);
                                            }
                                        });
                                    }
                                });

                                // show it
                                alertDialog.show();

                                new onSessionShareID().execute("get", null, null, null);
                            }
                        }, 200);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home: {
                finish();
                return true;
            }
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
            case R.id.menu_remove: {
                new onRemoveLock().execute();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onControl(View view){
        if(!inProgress) {
            new startControl().execute();
        }
    }

    private class startControl extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inProgress = true;
            unlockDrawable.setSpeed(5.0f);
            lockDrawable.setSpeed(5.0f);
        }

        @Override
        protected String doInBackground(Void... params) {
            String controlLock_url = "https://lockee-andrei-b.c9users.io/android/lock_mechanic/";
            // This is the login request
            try {
                URL url = new URL(controlLock_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userEmail", email);
                jsonParam.put("lockInnerID", lockInnerID);
                DataOutputStream bufferedWriter = new DataOutputStream(httpURLConnection.getOutputStream());
                bufferedWriter.writeBytes(jsonParam.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    if(result != null) {
                        unlockDrawable.setSpeed(1.0f);
                        lockDrawable.setSpeed(1.0f);
                        switch (result) {
                            case "#unlocked":
                                lockDrawable.seekTo(unlockDrawable.getCurrentPosition());
                                controlView.setImageDrawable(lockDrawable);
                                break;
                            case "#locked":
                                unlockDrawable.seekTo(lockDrawable.getCurrentPosition());
                                controlView.setImageDrawable(unlockDrawable);
                                break;
                            case "lock does not exist":
                                Toast.makeText(ControlActivity.this, "You removed the lock during this session", Toast.LENGTH_LONG).show();
                                finish();
                                break;
                        }
                        inProgress = false;
                    } else {
                        Log.e("LockControlHandler", "There was an error handling the control, please check connection");
                    }
                }
            }, 750);
        }
    }

    public void onGenerateShareID(View view){
        Button shareIDButton = (Button) findViewById(R.id.shareIDButton);
        final ObjectAnimator rotate = ObjectAnimator.ofFloat(shareIDButton, "rotation", 0f, 360f);
        rotate.setDuration(750);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(rotate);
        mAnimationSet.start();
        new startGenerateStaticShareID().execute();
    }

    public void onCopyStaticShareID(View view){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("staticShareID", shareIDText.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast toast = Toast.makeText(ControlActivity.this, "Static ShareID copied to your clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onCopyLockID(View view){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("lockID", lockIDText.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast toast = Toast.makeText(ControlActivity.this, "LockID copied to your clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onCopySessionShareID(View view){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("sessionShareID", sessionShareIDText.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast toast = Toast.makeText(ControlActivity.this, "Session ShareID copied to your clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    private class startGenerateStaticShareID extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String generateStaticShareID_url = "https://lockee-andrei-b.c9users.io/android/generate_static/";
            // This is the login request
            try {
                URL url = new URL(generateStaticShareID_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", email);
                jsonParam.put("nickname", nickname);
                DataOutputStream bufferedWriter = new DataOutputStream(httpURLConnection.getOutputStream());
                bufferedWriter.writeBytes(jsonParam.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                shareIDText.setText(result);
                Toast toast = Toast.makeText(ControlActivity.this, "ShareID generated\nTap the ID to copy it to your clipboard", Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setGravity(Gravity.CENTER);
                toast.show();
            } else {
                Log.e("GenerateShareIDHandler", "There was an error generating the share ID, please check connection");
            }
        }
    }

    private class onRemoveLock extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String removeLock_url = "https://lockee-andrei-b.c9users.io/android/remove/";
            // This is the login request
            try {
                URL url = new URL(removeLock_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", email);
                jsonParam.put("nickname", nickname);
                DataOutputStream bufferedWriter = new DataOutputStream(httpURLConnection.getOutputStream());
                bufferedWriter.writeBytes(jsonParam.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                switch(result){
                    case "success":
                        finish();
                        break;
                    default:
                        break;
                }
            } else {
                Log.e("LockRemoveHandler", "There was an error handling the lock removal, please check connection");
            }
        }
    }

    private class onSessionShareID extends AsyncTask<String, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> sessionShareInfo;

        android.app.AlertDialog progressDialog = new SpotsDialog(ControlActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            sessionDialog.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String type = params[0];
            String days = params[1];
            String hours = params[2];
            String minutes = params[3];

            String generateSession_url = "https://lockee-andrei-b.c9users.io/android/generate_session/";
            String getSession_url = "https://lockee-andrei-b.c9users.io/android/get_session/";

            String jsonStr = "";

            try {
                HttpURLConnection conn;


                if(type.equals("generate")) {
                    URL url = new URL(generateSession_url);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", email);
                    jsonParam.put("nickname", nickname);
                    jsonParam.put("days", days);
                    jsonParam.put("hours", hours);
                    jsonParam.put("minutes", minutes);
                    DataOutputStream bufferedWriter = new DataOutputStream(conn.getOutputStream());
                    bufferedWriter.writeBytes(jsonParam.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } else {
                    URL url = new URL(getSession_url);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", email);
                    jsonParam.put("nickname", nickname);
                    DataOutputStream bufferedWriter = new DataOutputStream(conn.getOutputStream());
                    bufferedWriter.writeBytes(jsonParam.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }

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

            sessionShareInfo = ParseJSON("session_share", jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if(sessionShareInfo != null) {
                        sessionShareIDText.setText(sessionShareID);
                        if(expirationDate.equals("Expired")){
                            expirationDateHolder.setText("This session has already expired");
                            expirationDateText.setVisibility(View.INVISIBLE);
                        } else {
                            expirationDateHolder.setText("This session ID expires on: ");
                            expirationDateText.setText(expirationDate);
                            expirationDateText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("SessionShareHandler", "There was an error handling the session share ID, please check connection");
                    }
                    sessionDialog.setVisibility(View.VISIBLE);
                }
            }, 750);
        }
    }

    private class getLockDetails extends AsyncTask<Void, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> lockDetails;

        android.app.AlertDialog progressDialog = new SpotsDialog(ControlActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            controlLayout.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String getLockDetails_url = "https://lockee-andrei-b.c9users.io/android/get_details/";

            String jsonStr = "";

            URL url;
            try {
                url = new URL(getLockDetails_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", email);
                jsonParam.put("lockInnerID", lockInnerID);
                DataOutputStream bufferedWriter = new DataOutputStream(conn.getOutputStream());
                bufferedWriter.writeBytes(jsonParam.toString());
                bufferedWriter.flush();
                bufferedWriter.close();

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

            lockDetails = ParseJSON("lock_details", jsonStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if(lockDetails != null) {
                        lockNicknameText.setText(nickname);
                        shareIDText.setText(staticShareID);
                        if(lockStatus.equals("#unlocked")){
                            controlView.setImageDrawable(lockDrawable);
                        } else {
                            controlView.setImageDrawable(unlockDrawable);
                        }
                        controlLayout.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(ControlActivity.this, "You removed the lock during this session", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }, 750);
        }
    }

    private ArrayList<HashMap<String, String>> ParseJSON (String type, String json) {
        if (json != null) {
            try {
                if(type.equals("lock_details")) {
                    ArrayList<HashMap<String, String>> lockInfo = new ArrayList<>();

                    JSONObject jsonObj = new JSONObject(json);

                    // Getting JSON Array node
                    JSONArray locks = jsonObj.getJSONArray(TAG_LOCK_DETAILS);

                    JSONObject c = locks.getJSONObject(0);

                    nickname = c.getString(TAG_NICKNAME);
                    lockStatus = c.getString(TAG_LOCK_STATUS);
                    staticShareID = c.getString(TAG_STATIC_SHARE_ID);

                    HashMap<String, String> lock = new HashMap<>();

                    lock.put(TAG_NICKNAME, nickname);
                    lock.put(TAG_LOCK_STATUS, lockStatus);
                    lock.put(TAG_STATIC_SHARE_ID, staticShareID);

                    lockInfo.add(lock);

                    return lockInfo;
                } else {
                    ArrayList<HashMap<String, String>> shareInfo = new ArrayList<>();

                    JSONObject jsonObj = new JSONObject(json);

                    // Getting JSON Array node
                    JSONArray locks = jsonObj.getJSONArray(TAG_SESSION_SHARE_INFO);

                    JSONObject c = locks.getJSONObject(0);

                    sessionShareID = c.getString(TAG_SESSION_SHARE_ID);
                    expirationDate = c.getString(TAG_EXPIRATION_DATE);

                    HashMap<String, String> share = new HashMap<>();

                    share.put(TAG_SESSION_SHARE_ID, sessionShareID);
                    share.put(TAG_EXPIRATION_DATE, expirationDate);

                    shareInfo.add(share);

                    return shareInfo;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("LockDetailsHandler", "There was an error getting the lock details, please check connection");
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getLockDetails().execute();
    }
}
