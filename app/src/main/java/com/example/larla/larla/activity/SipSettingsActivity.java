package com.example.larla.larla.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.larla.larla.R;
import com.example.larla.larla.sip.LarlaSipManager;

public class SipSettingsActivity extends AppCompatActivity {

    LarlaSipManager sipManager = LarlaSipManager.getInstance(this);

    String username = null, domain = null, password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(96, 96, 96, 96);

        if(sipManager.getProfile() != null) {
            username = sipManager.getProfile().getUserName();
            password = sipManager.getProfile().getPassword();
            domain = sipManager.getProfile().getSipDomain();
        }
        else {
            SharedPreferences preferences = getBaseContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            username = preferences.getString("sipUsername", null);
            password = preferences.getString("sipPassword", null);
            domain = preferences.getString("sipDomain", null);
        }

        final EditText userNameInput = new EditText(this);
        userNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        userNameInput.setHint("User name");
        userNameInput.setText(username);
        layout.addView(userNameInput);

        final EditText domainInput = new EditText(this);
        domainInput.setInputType(InputType.TYPE_CLASS_TEXT);
        domainInput.setHint("SIP domain");
        domainInput.setText(domain);
        layout.addView(domainInput);

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");
        passwordInput.setText(password);
        layout.addView(passwordInput);

        final Button save = new Button(this);
        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = userNameInput.getText().toString();
                String domain = domainInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
                    Toast.makeText(SipSettingsActivity.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                }

                SharedPreferences preferences = getBaseContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sipUsername", username);
                editor.putString("sipDomain", domain);
                editor.putString("sipPassword", password);
                editor.apply();

                sipManager.initializeManager(username, domain, password);
                Toast.makeText(SipSettingsActivity.this, "Credentials stored succesfully", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(save);

        setContentView(layout);
    }
}
