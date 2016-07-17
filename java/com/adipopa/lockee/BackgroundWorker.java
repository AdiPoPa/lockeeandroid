package com.adipopa.lockee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class BackgroundWorker extends AsyncTask<String, Void, String> {

    Intent i;
    AlertDialog alertDialog;
    TextView myMsg;
    Context context;

    public BackgroundWorker(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];
        String login_url = "https://lockee-app-adipopa.c9users.io/login.php";
        String register_url = "https://lockee-app-adipopa.c9users.io/register.php";
        if (!login_url.isEmpty() && !register_url.isEmpty()) {

            // This is the login request

            if (type.equals("login")) {
                try {
                    String email = params[1];
                    String password = params[2];
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String postData = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                            URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
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

            // This is the register request

            } else if(type.equals("register")){
                try {
                    String firstName = params[1];
                    String lastName = params[2];
                    String email = params[3];
                    String password = params[4];
                    URL url = new URL(register_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String postData = URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(firstName, "UTF-8") + "&" +
                            URLEncoder.encode("lastName", "UTF-8") + "=" + URLEncoder.encode(lastName, "UTF-8") + "&" +
                            URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                            URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
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
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        i = new Intent(context, MainActivity.class);
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Status");
        myMsg = new TextView(context);
        myMsg.setHeight(150);
        myMsg.setGravity(Gravity.CENTER);
        myMsg.setTextSize(18);
        myMsg.setTextColor(Color.BLACK);
    }

    @Override
    protected void onPostExecute(String result) {
        switch (result) {
            case "login success": {
                context.startActivity(i);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.emailField.setText("");
                        LoginActivity.passwordField.setText("");
                        LoginActivity.emailField.requestFocus();
                    }
                }, 250);
                break;
            }
            case "login not success": {
                myMsg.setText(R.string.wrongLogin);
                alertDialog.setView(myMsg);
                alertDialog.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.passwordField.setText("");
                        LoginActivity.passwordField.requestFocus();
                    }
                }, 250);
                break;
            }
            case "register success": {
                myMsg.setText(R.string.correctRegister);
                alertDialog.setView(myMsg);
                alertDialog.show();
                break;
            }
            case "register information error": {
                myMsg.setText(R.string.wrongRegister);
                alertDialog.setView(myMsg);
                alertDialog.show();
                break;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
