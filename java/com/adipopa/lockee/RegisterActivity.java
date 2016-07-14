package com.adipopa.lockee;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        // Convert DIP to pixels

        Resources r = getResources();
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400,
                r.getDisplayMetrics()
        );

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34,
                r.getDisplayMetrics()
        );

        // Set every field's width by pixels

        EditText firstNameField = (EditText)findViewById(R.id.firstNameField);
        firstNameField.setWidth(width);
        firstNameField.setHeight(height);

        EditText lastNameField = (EditText)findViewById(R.id.lastNameField);
        lastNameField.setWidth(width);
        lastNameField.setHeight(height);

        EditText emailField = (EditText)findViewById(R.id.emailField);
        emailField.setWidth(width);
        emailField.setHeight(height);

        EditText passwordField = (EditText)findViewById(R.id.passwordField);
        passwordField.setWidth(width);
        passwordField.setHeight(height);
    }
}