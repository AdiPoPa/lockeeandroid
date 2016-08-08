package com.adipopa.lockee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    TextView emailError, loginError;
    LinearLayout linearLayout;
    public static Activity login;
    static Boolean active = false;
    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        login = this;
        active = true;

        mainActivity = new MainActivity();

        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        emailError = (TextView) findViewById(R.id.emailError);
        loginError = (TextView) findViewById(R.id.loginError);

        linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);

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
                }
                else {
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
                } else{
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
                if(!b){
                    String email = emailField.getText().toString();
                    if(email.isEmpty()){
                        emptyField(emailField);
                    }
                }
            }
        });

        passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    String password = passwordField.getText().toString();
                    if(password.isEmpty()){
                        emptyField(passwordField);
                    }
                    else{
                        hideEmptyError(passwordField);
                    }
                }
            }
        });

        final TextView register = (TextView)findViewById(R.id.register);

        register.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        register.setTextColor(Color.parseColor("#3F51B5"));
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                register.setTextColor(Color.parseColor("#4799E8"));
                                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                                startActivity(i);
                            }
                        }, 200);
                    }
                }
        );

        final TextView guest = (TextView)findViewById(R.id.guest);

        guest.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        guest.setTextColor(Color.parseColor("#3F51B5"));
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                guest.setTextColor(Color.parseColor("#4799E8"));
                                SaveSharedPreference.setLoginStatus(LoginActivity.this, "logged in");
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }, 200);
                    }
                }
        );
    }

    public void onLogin(View view){
        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();
        String type = "login";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, email, password);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BackgroundWorker.loginStatus.equals("success")) {
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            SaveSharedPreference.setLoginStatus(LoginActivity.this, "logged in");
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }, 200);
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
                    }
                    else{
                        showError(emailField, emailError, "Please type a valid email address");
                        showError(emailField, loginError, "Please verify your input information");
                    }
                }
            }
        }, 750);
    }

    public void onReleaseFocus(View view){
        linearLayout.requestFocus();
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

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}
