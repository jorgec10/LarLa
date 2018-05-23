package com.example.larla.larla.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;

public class RegisterActivity extends AppCompatActivity {

    private RegisterTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button register = findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText textInputEmail = findViewById(R.id.register_email);
                TextInputEditText textInputUsername = findViewById(R.id.register_username);
                TextInputEditText textInputPassword = findViewById(R.id.register_password);
                TextInputEditText textInputPassword2 = findViewById(R.id.register_password2);

                String email = textInputEmail.getText().toString();
                String username = textInputUsername.getText().toString();
                String password = textInputPassword.getText().toString();
                String password2 = textInputPassword2.getText().toString();

                if (!password.equals(password2))
                    Toast.makeText(RegisterActivity.this, "Passwords are not equal!", Toast.LENGTH_SHORT).show();
                else {
                    task = new RegisterTask(password, username);
                    task.execute((Void) null);
                }


            }
        });


    }


    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPassword;
        private final String mUsername;

        RegisterTask(String password, String username) {
            mPassword = password;
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return Matrix.getInstance(getApplicationContext()).register(mPassword, mUsername);

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            task = null;

            if (success) {
                Toast.makeText(RegisterActivity.this, "Register done!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Register failed :(", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            task = null;
        }
    }
}
