package com.example.larla.larla.sip;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.sip.*;
import android.util.Log;


public class IncomingCallReceiver extends BroadcastReceiver {
    /**
     * Processes the incoming call, answers it, and hands it
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("Call", "onReceive...");

        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Log.d("Call", "receiving...");

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            final Ringtone ringtone = RingtoneManager.getRingtone(context, notification);
            ringtone.play();

            final SipAudioCall incomingCall = LarlaSipManager.getInstance(context).getManager().takeAudioCall(intent, listener);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(LarlaSipManager.getInstance(context).getManager().getSessionFor(intent).getPeerProfile().getUriString());
            dialogBuilder.setPositiveButton("Pick up", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        LarlaSipManager.getInstance(context).setCall(incomingCall);

                        incomingCall.answerCall(30);
                        incomingCall.startAudio();
                        incomingCall.setSpeakerMode(true);
                        if (incomingCall.isMuted()) {
                            incomingCall.toggleMute();
                        }

                        ringtone.stop();

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                        dialogBuilder.setTitle(LarlaSipManager.getInstance(context).getManager().getSessionFor(intent).getPeerProfile().getUriString());
                        dialogBuilder.setNegativeButton("Hang up", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    incomingCall.setSpeakerMode(false);
                                    incomingCall.endCall();
                                } catch (SipException e) {
                                    if (incomingCall != null) {
                                        incomingCall.close();
                                    }
                                }
                                LarlaSipManager.getInstance(context).setCall(null);
                                dialog.cancel();
                            }
                        });

                        dialogBuilder.show();

                    } catch (SipException e) {
                        if (incomingCall != null) {
                            incomingCall.close();
                        }
                    }

                }
            });

            dialogBuilder.setNegativeButton("Hang up", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        incomingCall.setSpeakerMode(false);
                        incomingCall.endCall();
                    } catch (SipException e) {
                        if (incomingCall != null) {
                            incomingCall.close();
                        }
                    }
                    ringtone.stop();

                    LarlaSipManager.getInstance(context).setCall(null);

                    dialog.cancel();
                }
            });

            dialogBuilder.show();

        } catch (SipException e) {
            e.printStackTrace();
        }
    }
}