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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
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

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    ListView mainList;
    TextView noLocks;
    public static String registerNotification = "hide";
    String email, nickname, lockInnerID, orientation;
    EditText nicknameField, lockIDField;
    TextView nicknameError, lockIDError;
    CoordinatorLayout mainLayout;
    AlertDialog alertDialog;

    private static final String TAG_LOCKINFO = "locksInfo";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_LOCK_ID = "lockInnerID";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ICON = "icon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(SaveSharedPreference.getLoginStatus(this).equals("not logged")) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        mainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        email = SaveSharedPreference.getLoginStatus(this);
        noLocks = (TextView) findViewById(R.id.noLocks);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final FrameLayout registerSuccessful = (FrameLayout) findViewById(R.id.registerNotification);

        if (registerNotification.equals("show")) {
            mainLayout.setVisibility(View.INVISIBLE);
            final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(registerSuccessful, "alpha", 0f, 1f);
            fadeIn.setDuration(2000);
            final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(registerSuccessful, "alpha",  1f, 0f);
            fadeOut.setDuration(2000);

            final AnimatorSet mAnimationSet = new AnimatorSet();

            mAnimationSet.play(fadeIn);

            mAnimationSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimationSet.setStartDelay(3000);
                    mAnimationSet.play(fadeOut);
                    mAnimationSet.start();
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            mAnimationSet.end();
                            registerNotification = "hide";
                            new getLocks().execute();
                        }
                    }, 5000);
                }
            });
            mAnimationSet.start();
        }

        mainList = (ListView) findViewById(R.id.mainList);
        mainList.addFooterView(new View(MainActivity.this));

        mainList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        TextView lockIDText = (TextView) view.findViewById(R.id.lockIDText);
                        Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                        String lockID = lockIDText.getText().toString();
                        intent.putExtra("lockID", lockID);
                        startActivity(intent);
                    }
                }
        );

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ViewGroup nullParent = null;
                LayoutInflater li = LayoutInflater.from(context);
                View dialogView = li.inflate(R.layout.addlock_dialog, nullParent);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.alertDialog);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(dialogView);

                nicknameField = (EditText) dialogView.findViewById(R.id.nicknameField);
                lockIDField = (EditText) dialogView.findViewById(R.id.lockIDField);

                nicknameError = (TextView) dialogView.findViewById(R.id.nicknameError);
                lockIDError = (TextView) dialogView.findViewById(R.id.lockIDError);

                final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.orientation);

                nicknameError.setVisibility(View.GONE);
                lockIDError.setVisibility(View.GONE);

                // set dialog message
                alertDialogBuilder.setPositiveButton("Add the lock", null);

                // create alert dialog
                alertDialog = alertDialogBuilder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                nickname = nicknameField.getText().toString();
                                lockInnerID = lockIDField.getText().toString();
                                RadioButton orientationButton = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                                orientation = orientationButton.getText().toString().toLowerCase();
                                if(nickname.isEmpty()) {
                                    emptyField(nicknameField);
                                }
                                if(lockInnerID.isEmpty()){
                                    emptyField(lockIDField);
                                }
                                if(!nickname.isEmpty() && !lockInnerID.isEmpty()){
                                    new onAddLock().execute();
                                }
                            }
                        });
                    }
                });

                // show it
                alertDialog.show();

                nicknameField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String nickname = nicknameField.getText().toString();
                        if (nickname.isEmpty()) {
                            emptyField(nicknameField);
                        } else {
                            hideError(nicknameField, nicknameError);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                lockIDField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String lockInnerID = lockIDField.getText().toString();
                        if (lockInnerID.isEmpty()) {
                            emptyField(lockIDField);
                        } else{
                            hideError(lockIDField, lockIDError);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                nicknameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(!b){
                            String nickname = nicknameField.getText().toString();
                            if(nickname.isEmpty()){
                                emptyField(nicknameField);
                            }
                        }
                    }
                });

                lockIDField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(!b){
                            String lockInnerID = lockIDField.getText().toString();
                            if(lockInnerID.isEmpty()){
                                emptyField(lockIDField);
                            }
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_sync: {
                new getLocks().execute();
                return true;
            }
            case R.id.menu_settings: {
                Toast toast = Toast.makeText(MainActivity.this, "This is a place holder\nFeature coming soon", Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setGravity(Gravity.CENTER);
                toast.show();
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
            case R.id.menu_logout: {
                SaveSharedPreference.setLoginStatus(MainActivity.this, "not logged");
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Async task class to get json by making HTTP call **/

    private class getLocks extends AsyncTask<Void, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> locksList;

        android.app.AlertDialog progressDialog = new SpotsDialog(MainActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            mainLayout.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Creating service handler class instance

            // Making a request to url and getting response
            String getLocks_url = "https://lockee-andrei-b.c9users.io/android/get_locks/";

            String jsonStr = "";

            try {
                URL url = new URL(getLocks_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                if (email != null) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", email);
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

            locksList = ParseJSON(jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if(locksList != null) {
                        if (locksList.size() > 0) {
                            ListAdapter adapter = new SimpleAdapter(
                                    MainActivity.this, locksList,
                                    R.layout.main_list, new String[]{TAG_NICKNAME, TAG_LOCK_ID, TAG_ICON},
                                    new int[]{R.id.nicknameText, R.id.lockIDText, R.id.lockIcon}
                            );
                            mainList.setAdapter(adapter);
                            noLocks.setVisibility(View.GONE);
                            mainList.setVisibility(View.VISIBLE);
                        } else {
                            noLocks.setVisibility(View.VISIBLE);
                            mainList.setVisibility(View.GONE);
                        }
                        mainLayout.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("LocksListHandler", "There was an error getting the list of locks, please check connection");
                    }
                }
            }, 750);
        }

    }

    private ArrayList<HashMap<String, String>> ParseJSON(String json) {
        if (json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> locksList = new ArrayList<>();

                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                JSONArray locks = jsonObj.getJSONArray(TAG_LOCKINFO);

                // looping through All Locks
                for (int i = 0; i < locks.length(); i++) {
                    JSONObject c = locks.getJSONObject(i);

                    String nickname = c.getString(TAG_NICKNAME);
                    String lock_id = c.getString(TAG_LOCK_ID);
                    String status = c.getString(TAG_STATUS);
                    int icon = 0;
                    if(status.equals("#unlocked")) {
                        icon = R.drawable.ic_lock_open_black_48dp;
                    } else if (status.equals("#locked")) {
                        icon = R.drawable.ic_lock_outline_black_48dp;
                    }

                    // tmp hashmap for single lock
                    HashMap<String, String> lock = new HashMap<>();

                    // adding each child node to HashMap key => value
                    lock.put(TAG_NICKNAME, nickname);
                    lock.put(TAG_LOCK_ID, lock_id);
                    lock.put(TAG_ICON, Integer.toString(icon));

                    // adding lock to locks list
                    locksList.add(lock);
                }
                return locksList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
            return null;
        }
    }

    private class onAddLock extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String addLock_url = "https://lockee-andrei-b.c9users.io/android/add_lock/";
            // This is the login request
            try {
                URL url = new URL(addLock_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", email);
                jsonParam.put("nickname", nickname);
                jsonParam.put("lockInnerID", lockInnerID);
                jsonParam.put("orientation", orientation);
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
                switch (result) {
                    case "lock registered":
                        hideError(nicknameField, nicknameError);
                        hideError(lockIDField, lockIDError);
                        alertDialog.hide();
                        new getLocks().execute();
                        break;
                    case "nickname already used":
                        nicknameField.requestFocus();
                        showError(nicknameField, nicknameError, "You've already used this nickname");
                        break;
                    case "this lock was already registered":
                        lockIDField.requestFocus();
                        showError(lockIDField, lockIDError, "You've already registered this lock");
                        break;
                    case "lock does not exist":
                        lockIDField.requestFocus();
                        showError(lockIDField, lockIDError, "This lock doesn't exist in the database");
                        break;
                    default:
                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                Log.e("AddLockHandler", "There was an error registering the lock, please check connection");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(registerNotification.equals("hide")) {
            new getLocks().execute();
        }
    }

    public void emptyField(EditText editText){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    // Method to show a error with a custom string message

    public void showError(EditText editText, TextView textView, String string){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
    }

    // Method to hide one of the errors defined above

    public void hideError(EditText editText, TextView textView){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ResourcesCompat.getDrawable(getResources(), R.mipmap.tick, null), null);
        textView.setVisibility(View.GONE);
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
