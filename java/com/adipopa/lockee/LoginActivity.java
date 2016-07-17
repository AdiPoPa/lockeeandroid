package com.adipopa.lockee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    public static EditText emailField;
    public static EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        emailField = (EditText)findViewById(R.id.emailField);
        passwordField = (EditText)findViewById(R.id.passwordField);

        final TextView register = (TextView)findViewById(R.id.register);

        register.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        register.setTextColor(Color.GRAY);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                register.setTextColor(Color.WHITE);
                                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                                startActivity(i);
                            }
                        }, 200);
                    }
                }
        );
    }

    public void onLogin(View view){
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String type = "login";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, email, password);
    }
}
