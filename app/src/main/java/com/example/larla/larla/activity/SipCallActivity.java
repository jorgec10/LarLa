package com.example.larla.larla.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larla.larla.R;
import com.example.larla.larla.sip.IncomingCallReceiver;
import com.example.larla.larla.sip.LarlaSipManager;

import org.w3c.dom.Text;

import java.text.ParseException;

public class SipCallActivity extends Activity {

    private String sipAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sip_call);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        IncomingCallReceiver callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

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
