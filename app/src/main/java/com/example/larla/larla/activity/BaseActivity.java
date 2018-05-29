package com.example.larla.larla.activity;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.larla.larla.sip.IncomingCallReceiver;

public class BaseActivity extends AppCompatActivity {

    private IncomingCallReceiver callReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("BaseActivity", "onPause: ");
        this.unregisterReceiver(callReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("BaseActivity", "onResume: ");
        this.registerReceiver(callReceiver, filter);
    }
}
