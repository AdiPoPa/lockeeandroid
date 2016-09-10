package com.adipopa.lockee;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

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

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    final Context context = this;
    public static Activity loginActivity;
    EditText emailField, passwordField, shareIDField;
    TextView emailError, loginError, shareIDError;
    AlertDialog alertDialog;
    LinearLayout linearLayout;
    String email, password, shareID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        loginActivity = this;

        MainActivity mainActivity = new MainActivity();
        mainActivity.finish();

        if(!SaveSharedPreference.getSharedID(this).equals("not shared")) {
            Intent i = new Intent(LoginActivity.this, SharedControlActivity.class);
            startActivity(i);
            finish();
        } else {
            emailField = (EditText) findViewById(R.id.emailField);
            passwordField = (EditText) findViewById(R.id.passwordField);

            emailError = (TextView) findViewById(R.id.emailError);
            loginError = (TextView) findViewById(R.id.loginError);

            linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

            emailError.setVisibility(View.GONE);
            loginError.setVisibility(View.GONE);

            emailField.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String email = emailField.getText().toString();
                    hideError(emailField, loginError);
                    if (email.isEmpty()) {
                        emptyError(emailField, emailError);
                    } else if (!isEmailValid(email)) {
                        showError(emailField, emailError, "Please type a valid email address");
                    } else {
                        hideError(emailField, emailError);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            passwordField.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String password = passwordField.getText().toString();
                    if (password.isEmpty()) {
                        emptyField(passwordField);
                    } else {
                        hideError(passwordField, loginError);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        String email = emailField.getText().toString();
                        if (email.isEmpty()) {
                            emptyField(emailField);
                        }
                    }
                }
            });

            passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        String password = passwordField.getText().toString();
                        if (password.isEmpty()) {
                            emptyField(passwordField);
                        }
                    }
                }
            });

            final TextView register = (TextView) findViewById(R.id.register);

            register.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            register.setTextColor(Color.parseColor("#3F51B5"));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    register.setTextColor(Color.parseColor("#4799E8"));
                                    emailField.setBackground(
                                            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
                                    );
                                    passwordField.setBackground(
                                            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
                                    );
                                    Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                                    startActivity(i);
                                }
                            }, 200);
                        }
                    }
            );

            final TextView guest = (TextView) findViewById(R.id.guest);

            guest.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            guest.setTextColor(Color.parseColor("#3F51B5"));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    guest.setTextColor(Color.parseColor("#4799E8"));

                                    emailField.setBackground(
                                            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
                                    );
                                    passwordField.setBackground(
                                            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
                                    );

                                    final ViewGroup nullParent = null;
                                    LayoutInflater li = LayoutInflater.from(context);
                                    View dialogView = li.inflate(R.layout.share_dialog, nullParent);

                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.alertDialog);

                                    // set prompts.xml to alertdialog builder
                                    alertDialogBuilder.setView(dialogView);

                                    shareIDField = (EditText) dialogView.findViewById(R.id.shareIDField);
                                    shareIDError = (TextView) dialogView.findViewById(R.id.shareIDError);
                                    shareIDError.setVisibility(View.GONE);

                                    // set dialog message
                                    alertDialogBuilder.setPositiveButton("Use the lock", null);

                                    // create alert dialog
                                    alertDialog = alertDialogBuilder.create();

                                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                            b.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    shareID = shareIDField.getText().toString();
                                                    if (shareID.isEmpty()) {
                                                        emptyField(shareIDField);
                                                    }
                                                    if (!shareID.isEmpty()) {
                                                        new onSharedLock().execute();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    // show it
                                    alertDialog.show();

                                    shareIDField.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                            String lockInnerID = shareIDField.getText().toString();
                                            if (lockInnerID.isEmpty()) {
                                                emptyField(shareIDField);
                                            } else {
                                                hideError(shareIDField, shareIDError);
                                            }
                                        }

                                        @Override
                                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                        }

                                        @Override
                                        public void afterTextChanged(Editable editable) {

                                        }
                                    });

                                    shareIDField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View view, boolean b) {
                                            if (!b) {
                                                String lockInnerID = shareIDField.getText().toString();
                                                if (lockInnerID.isEmpty()) {
                                                    emptyField(shareIDField);
                                                }
                                            }
                                        }
                                    });
                                }
                            }, 200);
                        }
                    }
            );
        }
    }

    public void onLogin(View view){
        email = emailField.getText().toString();
        password = passwordField.getText().toString();
        if(email.isEmpty()) {
            emptyField(emailField);
        }
        if(password.isEmpty()) {
            emptyField(passwordField);
        }
        if(!email.isEmpty() && isEmailValid(email) && !password.isEmpty()) {
            new startLogin().execute();
        }
    }

    private class startLogin extends AsyncTask<Void, Void, String> {

        android.app.AlertDialog progressDialog = new SpotsDialog(LoginActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String login_url = "https://lockee-andrei-b.c9users.io/android/login/";
            // This is the login request
            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String postData = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
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
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if(result != null) {
                        if(result.equals("login success")) {
                            SaveSharedPreference.setLoginStatus(LoginActivity.this, email);
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            passwordField.setText("");
                            emptyField(passwordField);
                            if (!email.isEmpty() || !isEmailValid(email)) {
                                emailField.requestFocus();
                            }
                            if (email.isEmpty()) {
                                emailField.requestFocus();
                            }
                            if (isEmailValid(email)) {
                                hideEmptyError(emailField);
                                passwordField.requestFocus();
                                showError(passwordField, loginError, "Please verify your input information");
                            } else {
                                showError(emailField, emailError, "Please type a valid email address");
                                showError(emailField, loginError, "Please verify your input information");
                            }
                        }
                    } else {
                        Log.e("LoginHandler", "There was an error handling the login, please check connection");
                    }
                }
            }, 750);
        }
    }

    private class onSharedLock extends AsyncTask<Void, Void, String> {

        android.app.AlertDialog progressDialog = new SpotsDialog(LoginActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String shareCheck_url = "https://lockee-andrei-b.c9users.io/android/share_check/";
            // This is the login request
            try {
                URL url = new URL(shareCheck_url);
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
                    if (result != null) {
                        switch (result) {
                            case "success":
                                SaveSharedPreference.setSharedID(LoginActivity.this, shareID);
                                Intent i = new Intent(LoginActivity.this, SharedControlActivity.class);
                                startActivity(i);
                                finish();
                                break;
                            case "wrong code":
                                shareIDField.requestFocus();
                                showError(shareIDField, shareIDError, "This share ID doesn't exist");
                                break;
                            case "expired":
                                shareIDField.requestFocus();
                                showError(shareIDField, shareIDError, "This share ID has expired");
                                break;
                            default:
                                break;
                        }
                    } else {
                        Log.e("AddSharedLockHandler", "There was an error registering the shared lock, please check connection");
                    }
                }
            }, 750);
        }
    }

    public void onReleaseFocus(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        (findViewById(R.id.dummy_id)).requestFocus();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Methods to show a error in case a field is empty

    public void emptyError(EditText editText, TextView textView){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        textView.setVisibility(View.GONE);
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

    // Method to hide the error in case a field is empty

    public void hideEmptyError(EditText editText){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ResourcesCompat.getDrawable(getResources(), R.mipmap.tick, null), null);
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
