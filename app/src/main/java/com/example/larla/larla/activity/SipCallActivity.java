package com.example.larla.larla.activity;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larla.larla.R;
import com.example.larla.larla.sip.IncomingCallReceiver;
import com.example.larla.larla.sip.LarlaSipManager;

public class SipCallActivity extends BaseActivity {

    private String sipAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sip_call);

        Button callButton = findViewById(R.id.buttonCall);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView receiverUserName = findViewById(R.id.textInputReceiverUserName);
                TextView receiverDomain = findViewById(R.id.textInputDomain);
                sipAddress = receiverUserName.getText().toString() + "@" + receiverDomain.getText().toString();
                Toast.makeText(SipCallActivity.this, sipAddress, Toast.LENGTH_SHORT).show();

                LarlaSipManager.getInstance(getApplicationContext()).initiateCall(sipAddress);

            }
        });

        Button endCallButton = findViewById(R.id.buttonEndCall);
        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LarlaSipManager.getInstance(getApplicationContext()).endCall();

            }
        });
    }


}
