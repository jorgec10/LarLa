package com.example.larla.larla.sip;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.util.Log;
import android.widget.Toast;

import com.example.larla.larla.activity.SipCallActivity;

import java.text.ParseException;

public class LarlaSipManager {

    private static LarlaSipManager ourInstance;

    private final Context context;

    private SipManager manager = null;
    private SipProfile me = null;
    private SipAudioCall call = null;

    public static LarlaSipManager getInstance(Context context) {

        if (ourInstance == null && context != null) {
            ourInstance = new LarlaSipManager(context);
        }
        return ourInstance;
    }

    private LarlaSipManager(Context context) {
        this.context = context;
    }

    public void initializeLocalProfile(String username, String domain, String password) {
        if (manager == null) {
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            me = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            Log.d("Call: ", "initializeLocalProfile: " + context);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);

            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
            manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    //updateStatus("Registering with SIP Server...");
                    //Toast.makeText(SipCallActivity.this, "Registering...", Toast.LENGTH_SHORT).show();
                    Log.d("Register", "Registering");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    //updateStatus("Ready");
                    //Toast.makeText(SipCallActivity.this, "Registration done...", Toast.LENGTH_SHORT).show();
                    Log.d("Register", "Register done");
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    //updateStatus("Registration failed.  Please check settings.");
                    //Toast.makeText(SipCallActivity.this, "Registration failed" + errorMessage, Toast.LENGTH_SHORT).show();
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


    public void initializeManager(String username, String domain, String password) {
        if(manager == null) {
            manager = SipManager.newInstance(context);
        }
        initializeLocalProfile(username,domain,password);
    }

    public void initiateCall(final String sipAddress) {

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
                    Log.d("Call", "onCallEnded");
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

    public void endCall() {

        try {
            call.endCall();
        } catch (SipException e) {
            if (call != null) {
                call.close();
            }
        }
    }

    public SipManager getManager() {
        return manager;
    }

    public void setCall(SipAudioCall call) {
        this.call = call;
    }

    public SipProfile getProfile() {
        return me;
    }
}
