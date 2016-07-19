package com.adipopa.lockee;


import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    EditText nameField, emailField, passwordField, confirmPasswordField;
    TextView nameError, emailError, passwordError, confirmPasswordError;
    CheckBox termsCheckBox, errorCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        nameField = (EditText) findViewById(R.id.nameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        confirmPasswordField = (EditText) findViewById(R.id.confirmPasswordField);

        termsCheckBox = (CheckBox) findViewById(R.id.termsCheckBox);
        errorCheckBox = (CheckBox) findViewById(R.id.errorCheckBox);

        nameError = (TextView) findViewById(R.id.nameError);
        emailError = (TextView) findViewById(R.id.emailError);
        passwordError = (TextView) findViewById(R.id.passwordError);
        confirmPasswordError = (TextView) findViewById(R.id.confirmPasswordError);

        setErrorTextsDefault();

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String name = nameField.getText().toString().trim();
                String namePattern2 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                String namePattern3 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                String namePattern4 = "[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+ +[A-Z]+[a-z]+";
                if(name.isEmpty()){
                    emptyError(nameField, nameError);
                }else if(!name.matches(namePattern2) && !name.matches(namePattern3) && !name.matches(namePattern4)){
                    showError(nameField, nameError, "We'd like to know you, give us your real name");
                }else{
                    hideError(nameField, nameError);
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
                    emptyError(emailField, emailError);
                } else if (!isEmailValid(email)) {
                    showError(emailField, emailError, "Please type a valid email address");
                }
                else {
                    VerifyWorker verifyWorker = new VerifyWorker(this);
                    verifyWorker.execute("verify", email);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (VerifyWorker.emailStatus.equals("email available")) {
                                hideError(emailField, emailError);
                            } else {
                                showError(emailField, emailError, "This email is associated with another account");
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
                    emptyError(passwordField, passwordError);
                } else if (!password.isEmpty() && !confirmPassword.isEmpty()){
                    if (password.equals(confirmPassword)) {
                        hideError(confirmPasswordField, confirmPasswordError);
                    } else if (!password.equals(confirmPassword)) {
                        showError(confirmPasswordField, confirmPasswordError, "The password doesn't match the one above");
                     }
                }
                if (!password.isEmpty() && password.length() < 8) {
                    showError(passwordField, passwordError, "Your password must be at least 8 characters long");
                } else if (password.matches("[a-z]+")) {
                    showError(passwordField, passwordError, "Your password must not contain only small letters");
                } else if (password.matches("[0-9]+")) {
                    showError(passwordField, passwordError, "Your password must contain at least a letter");
                } else {
                    hideError(passwordField, passwordError);
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
                    emptyError(confirmPasswordField, confirmPasswordError);
                }else if(!password.equals(confirmPassword)){
                    showError(confirmPasswordField, confirmPasswordError, "The password doesn't match the one above");
                }else {
                    hideError(confirmPasswordField, confirmPasswordError);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        errorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                errorCheckBox.setVisibility(View.GONE);
                errorCheckBox.setChecked(false);
                termsCheckBox.setVisibility(View.VISIBLE);
                termsCheckBox.setChecked(true);
            }
        });
    }

    // Method called when the register button is pressed

    public void onRegister(View view){
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String type = "register";

        if(name.isEmpty()){
            emptyError(nameField, nameError);
        }
        if(email.isEmpty()){
            emptyError(emailField, emailError);
        }
        if(password.isEmpty()){
            emptyError(passwordField, passwordError);
        }
        if(confirmPassword.isEmpty()){
            emptyError(confirmPasswordField, confirmPasswordError);
        }

        if(!termsCheckBox.isChecked()) {
            termsCheckBox.setVisibility(View.GONE);
            errorCheckBox.setVisibility(View.VISIBLE);
        }
        else if(!nameError.isShown() && !emailError.isShown() && !passwordError.isShown() && !confirmPasswordError.isShown() &&
                !name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()){
            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, name, email, password, confirmPassword);
        }
    }

    // Method to check if email field is valid

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method to show a error in case a field is empty

    public void emptyError(EditText editText, TextView textView){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        textView.setHeight(16);
        textView.setVisibility(View.INVISIBLE);
    }

    // Method to show a error with a custom string message

    public void showError(EditText editText, TextView textView, String string){
        editText.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error, null)
        );
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        textView.setHeight(54);
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
        textView.setHeight(16);
        textView.setVisibility(View.INVISIBLE);
    }

    // Method to set the error texts invisible and set the default height

    public void setErrorTextsDefault(){
        nameError.setHeight(16);
        nameError.setVisibility(View.INVISIBLE);
        emailError.setHeight(16);
        emailError.setVisibility(View.INVISIBLE);
        passwordError.setHeight(16);
        passwordError.setVisibility(View.INVISIBLE);
        confirmPasswordError.setHeight(16);
        confirmPasswordError.setVisibility(View.INVISIBLE);
    }
}