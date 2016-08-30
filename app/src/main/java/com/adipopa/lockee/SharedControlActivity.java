package com.adipopa.lockee;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import dmax.dialog.SpotsDialog;
import pl.droidsonroids.gif.GifImageView;

public class SharedControlActivity extends AppCompatActivity {

    final Context context = this;
    GifImageView unlockView, lockView;
    String nickname, shareID;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_control_screen);

        LoginActivity loginActivity = new LoginActivity();
        loginActivity.finish();

        nickname = SaveSharedPreference.getSharedNickname(this);
        shareID = SaveSharedPreference.getSharedID(this);

        unlockView = (GifImageView) findViewById(R.id.unlockView);
        lockView = (GifImageView) findViewById(R.id.lockView);

        unlockView.setAlpha(0f);
        lockView.setAlpha(0f);

        TextView lockNicknameText = (TextView) findViewById(R.id.lockNickname);
        TextView shareIDText = (TextView) findViewById(R.id.shareID);

        lockNicknameText.setText(nickname);
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
                new onVerifyStatus().execute(shareID);
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
        TextView shareID = (TextView) findViewById(R.id.shareID);
        String share_id = shareID.getText().toString();
        new startControl().execute(share_id);
    }

    private class startControl extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String shareID = params[0];
            String share_mechanic_url = "https://lockee-cloned-andrei-b.c9users.io/android/share_mechanic/";
            // This is the login request
            try {
                URL url = new URL(share_mechanic_url);
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
        protected void onPostExecute(String result) {
            if(result != null) {
                if(result.equals("unlocked")){
                    unlockView.setAlpha(0f);
                    lockView.setAlpha(1f);
                } else {
                    unlockView.setAlpha(1f);
                    lockView.setAlpha(0f);
                }
            } else {
                Log.e("SharedControlHandler", "There was an error handling the control, please check connection");
            }
        }
    }

    private class onVerifyStatus extends AsyncTask<String, Void, String> {

        android.app.AlertDialog progressDialog = new SpotsDialog(SharedControlActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String shareID = params[0];
            String share_ping_url = "https://lockee-cloned-andrei-b.c9users.io/android/share_ping/";
            // This is the login request
            try {
                URL url = new URL(share_ping_url);
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
                    progressDialog.dismiss();

                    if(result != null) {
                        if(result.equals("unlocked")){
                            unlockView.setAlpha(0f);
                            lockView.setAlpha(1f);
                        } else {
                            unlockView.setAlpha(1f);
                            lockView.setAlpha(0f);
                        }
                    } else {
                        Log.e("SharedVerifyHandler", "There was an error verifying the shared lock, please check connection");
                    }
                }
            }, 1000);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new onVerifyStatus().execute(shareID);
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
