package com.adipopa.lockee;


import android.os.AsyncTask;
import android.text.TextWatcher;

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

public class VerifyWorker extends AsyncTask<String, Integer, String> {

    TextWatcher watcher;
    public static String emailStatus = "email available";

    public VerifyWorker(TextWatcher textWatcher) {
        watcher = textWatcher;
    }

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];
        String verify_url = "https://lockee-app-adipopa.c9users.io/verify.php";
        if (!verify_url.isEmpty()) {
            if (type.equals("verify")) {
                try {
                    String email = params[1];
                    URL url = new URL(verify_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String postData = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
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
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
            emailStatus = result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate();
    }
}
