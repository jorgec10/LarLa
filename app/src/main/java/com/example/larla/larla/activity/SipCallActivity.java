package com.example.larla.larla.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.w3c.dom.Text;

import java.text.ParseException;

public class SipCallActivity extends Activity {

    private SipManager manager = null;
    private SipProfile me = null;
    private SipAudioCall call = null;
    private String username, domain, password, sipAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sip_call);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Introduce SIP credentials");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(96, 96, 96, 96);

        final EditText userNameInput = new EditText(this);
        userNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        userNameInput.setHint("User name");
        layout.addView(userNameInput);

        final EditText domainInput = new EditText(this);
        domainInput.setInputType(InputType.TYPE_CLASS_TEXT);
        domainInput.setHint("SIP domain");
        layout.addView(domainInput);

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");
        layout.addView(passwordInput);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = userNameInput.getText().toString();
                domain = domainInput.getText().toString();
                password = passwordInput.getText().toString();

                if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
                    Toast.makeText(SipCallActivity.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                    
                    dialog.cancel();
                    try {
                        SipCallActivity.this.finish();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                
                TextView sipView = findViewById(R.id.textViewSipCredentials);
                sipView.setText(username + "@" + domain);
                initializeManager();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialogBuilder.setView(layout);
        dialogBuilder.show();

        Button callButton = findViewById(R.id.buttonCall);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView receiverUserName = findViewById(R.id.textInputReceiverUserName);
                TextView receiverDomain = findViewById(R.id.textInputDomain);
                sipAddress = receiverUserName.getText().toString() + "@" + receiverDomain.getText().toString();
                Toast.makeText(SipCallActivity.this, sipAddress, Toast.LENGTH_SHORT).show();

                initiateCall();

            }
        });
    }

    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            me = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);

            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
            manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    //updateStatus("Registering with SIP Server...");
                    Toast.makeText(SipCallActivity.this, "Registering...", Toast.LENGTH_SHORT).show();
                    Log.d("Register", "Registering");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    //updateStatus("Ready");
                    Toast.makeText(SipCallActivity.this, "Registration done...", Toast.LENGTH_SHORT).show();
                    Log.d("Register", "Register done");
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    //updateStatus("Registration failed.  Please check settings.");
                    Toast.makeText(SipCallActivity.this, "Registration failed" + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.d("Register", "Register failed" + errorMessage);
                }
            });
        } catch (ParseException pe) {
            //updateStatus("Connection Error.");
            Log.d("Register", "Parse exception");
        } catch (SipException se) {
            //updateStatus("Connection error.");
            Log.d("Register", "Parse exception");
            se.printStackTrace();

        }
    }


    public void initializeManager() {
        if(manager == null) {
            manager = SipManager.newInstance(this);
        }
        initializeLocalProfile();
    }

    public void initiateCall() {

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.

                @Override
                public void onCalling(SipAudioCall call) {
                    Log.d("Call", "onCalling..." + sipAddress);
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    Log.d("Call", "onCallEstablished..." + sipAddress);
                    call.startAudio();
                    call.setSpeakerMode(true);
                    Log.d("Call", "onCallEstablished..." + call.isMuted());
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    //updateStatus("Ready.");
                }
            };

            call = manager.makeAudioCall(me.getUriString(), sipAddress, listener, 30);

        }
        catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }
}
