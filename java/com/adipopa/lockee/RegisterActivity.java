package com.adipopa.lockee;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    EditText nameField, emailField, passwordField, confirmPasswordField;
    TextView nameText, emailText, passwordText, confirmPasswordText;
    CheckBox termsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        nameField = (EditText) findViewById(R.id.nameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        confirmPasswordField = (EditText) findViewById(R.id.confirmPasswordField);
        termsCheckBox = (CheckBox) findViewById(R.id.termsCheckBox);

        nameText = (TextView) findViewById(R.id.nameText);
        emailText = (TextView) findViewById(R.id.emailText);
        passwordText = (TextView) findViewById(R.id.passwordText);
        confirmPasswordText = (TextView) findViewById(R.id.confirmPasswordText);

        setErrorTextsDefault();

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String name = nameField.getText().toString().trim();
                String namePattern2 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                String namePattern3 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                String namePattern4 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                if(name.isEmpty()){
                    showErrorBorder(nameField);
                    hideTick(nameField);
                    nameText.setHeight(16);
                    nameText.setVisibility(View.INVISIBLE);
                }else if(!name.matches(namePattern2) && !name.matches(namePattern3) && !name.matches(namePattern4)){
                    showErrorBorder(nameField);
                    hideTick(nameField);
                    nameText.setHeight(54);
                    nameText.setVisibility(View.VISIBLE);
                    nameText.setText("We'd like to know you, give us your real name");
                }else{
                    hideErrorBorder(nameField);
                    showTick(nameField);
                    nameText.setHeight(16);
                    nameText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String email = emailField.getText().toString();
                if (email.equals("")) {
                    emailText.setHeight(16);
                    emailText.setVisibility(View.INVISIBLE);
                    hideTick(emailField);
                    showErrorBorder(emailField);
                } else if (!isEmailValid(email)) {
                    emailText.setHeight(54);
                    emailText.setVisibility(View.VISIBLE);
                    emailText.setText("Please type a valid email address");
                    hideTick(emailField);
                    showErrorBorder(emailField);
                }
                else {
                    VerifyWorker verifyWorker = new VerifyWorker(this);
                    verifyWorker.execute("verify", email);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (VerifyWorker.emailStatus.equals("email available")) {
                                emailText.setHeight(16);
                                emailText.setVisibility(View.INVISIBLE);
                                showTick(emailField);
                                hideErrorBorder(emailField);
                            } else if (VerifyWorker.emailStatus.equals("email taken")) {
                                emailText.setHeight(54);
                                emailText.setVisibility(View.VISIBLE);
                                emailText.setText("This email is associated with another account");
                                hideTick(emailField);
                                showErrorBorder(emailField);
                            }
                        }
                    }, 500);
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
                String confirmPassword = confirmPasswordField.getText().toString();
                if (password.isEmpty()) {
                    showErrorBorder(passwordField);
                    hideTick(passwordField);
                    passwordText.setHeight(16);
                    passwordText.setVisibility(View.INVISIBLE);
                } else if (!password.isEmpty() && !confirmPassword.isEmpty()){
                    if (password.equals(confirmPassword)) {
                        hideErrorBorder(confirmPasswordField);
                        showTick(confirmPasswordField);
                        confirmPasswordText.setHeight(16);
                        confirmPasswordText.setVisibility(View.INVISIBLE);
                    } else if (!password.equals(confirmPassword)) {
                        showErrorBorder(confirmPasswordField);
                        hideTick(confirmPasswordField);
                        confirmPasswordText.setHeight(54);
                        confirmPasswordText.setVisibility(View.VISIBLE);
                        confirmPasswordText.setText("The password doesn't match the one above");
                     }
                }
                if (!password.isEmpty() && password.length() < 8) {
                    showErrorBorder(passwordField);
                    hideTick(passwordField);
                    passwordText.setHeight(54);
                    passwordText.setVisibility(View.VISIBLE);
                    passwordText.setText("Your password must be at least 8 characters long");
                } else if (password.matches("[a-z]+")) {
                    showErrorBorder(passwordField);
                    hideTick(passwordField);
                    passwordText.setHeight(54);
                    passwordText.setVisibility(View.VISIBLE);
                    passwordText.setText("Your password must not contain only small letters");
                } else if (password.matches("[0-9]+")) {
                    showErrorBorder(passwordField);
                    hideTick(passwordField);
                    passwordText.setHeight(54);
                    passwordText.setVisibility(View.VISIBLE);
                    passwordText.setText("Your password must contain at least a letter");
                } else {
                    hideErrorBorder(passwordField);
                    showTick(passwordField);
                    passwordText.setHeight(16);
                    passwordText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirmPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = passwordField.getText().toString();
                String confirmPassword = confirmPasswordField.getText().toString();
                if(confirmPassword.isEmpty()) {
                    showErrorBorder(confirmPasswordField);
                    hideTick(confirmPasswordField);
                    confirmPasswordText.setHeight(16);
                    confirmPasswordText.setVisibility(View.INVISIBLE);
                }else if(!password.equals(confirmPassword)){
                    showErrorBorder(confirmPasswordField);
                    hideTick(confirmPasswordField);
                    confirmPasswordText.setHeight(54);
                    confirmPasswordText.setVisibility(View.VISIBLE);
                    confirmPasswordText.setText("The password doesn't match the one above");
                }else {
                    hideErrorBorder(confirmPasswordField);
                    showTick(confirmPasswordField);
                    confirmPasswordText.setHeight(16);
                    confirmPasswordText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void onRegister(View view){
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String type = "register";

        setErrorTextsDefault();

        AlertDialog alertDialog;
        TextView myMsg;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Status");
        myMsg = new TextView(this);
        myMsg.setHeight(150);
        myMsg.setGravity(Gravity.CENTER);
        myMsg.setTextSize(18);
        myMsg.setTextColor(Color.BLACK);


        if(confirmPassword.equals(password) && termsCheckBox.isChecked()) {
            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, name, email, password, confirmPassword);
        }
        else if(!confirmPassword.equals(password)){
            confirmPasswordText.setHeight(54);
            confirmPasswordText.setVisibility(View.VISIBLE);
            if(confirmPassword.isEmpty())
                confirmPasswordText.setText("Please confirm the password");
            else
                confirmPasswordText.setText("The password doesn't match the one above");
        }
        else if(!termsCheckBox.isChecked()){
            myMsg.setText(R.string.termsNotChecked);
            alertDialog.setView(myMsg);
            alertDialog.show();
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void hideErrorBorder(EditText editText){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_style, null)
        );
    }

    public void showErrorBorder(EditText editText){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
    }

    public void showTick(TextView textView){
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ResourcesCompat.getDrawable(getResources(), R.mipmap.tick, null), null);
    }

    public void hideTick(TextView textView){
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    public void setErrorTextsDefault(){
        nameText.setHeight(16);
        nameText.setVisibility(View.INVISIBLE);
        emailText.setHeight(16);
        emailText.setVisibility(View.INVISIBLE);
        passwordText.setHeight(16);
        passwordText.setVisibility(View.INVISIBLE);
        confirmPasswordText.setHeight(16);
        confirmPasswordText.setVisibility(View.INVISIBLE);
    }
}