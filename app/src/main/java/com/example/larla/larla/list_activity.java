package com.example.larla.larla;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class list_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_test_activity);

        String user = this.getIntent().getStringExtra("user");
        String info = this.getIntent().getStringExtra("info");
        TextView text = (TextView) findViewById(R.id.textTest);
        text.setText(user + " is a " + info);

    }
}
