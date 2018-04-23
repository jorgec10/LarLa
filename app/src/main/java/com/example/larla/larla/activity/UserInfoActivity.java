package com.example.larla.larla.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;

import org.matrix.androidsdk.MXSession;

public class UserInfoActivity extends AppCompatActivity {

    MXSession session;
    String userId;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        setTitle("LarLa - User info");

        session = Matrix.getInstance(getApplicationContext()).getSession();

        userId = session.getDataHandler().getUserId();
        userName = session.getDataHandler().getUser(userId).displayname;

        TextView textViewUserId = findViewById(R.id.textViewUserId);
        textViewUserId.setText(userId);
        TextView textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserName.setText(userName);

    }
}
