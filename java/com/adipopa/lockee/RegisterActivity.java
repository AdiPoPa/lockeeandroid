package com.adipopa.lockee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    public static EditText firstNameField, lastNameField, emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        firstNameField = (EditText)findViewById(R.id.firstNameField);
        lastNameField = (EditText)findViewById(R.id.lastNameField);
        emailField = (EditText)findViewById(R.id.emailField);
        passwordField = (EditText)findViewById(R.id.passwordField);


    }

    public void onRegister(View view){
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String type = "register";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, firstName, lastName, email, password);
    }
}
