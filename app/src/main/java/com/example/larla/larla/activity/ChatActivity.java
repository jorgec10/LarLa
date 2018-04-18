package com.example.larla.larla.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.sync.RoomResponse;


import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_test_activity);

        final String roomId = this.getIntent().getStringExtra("roomId");

        final ChatView chatView = (ChatView) findViewById(R.id.chat_view);

        final MXSession session = Matrix.getInstance(getApplicationContext()).getSession();
        session.getDataHandler().getStore().getRoomMessages(roomId);
        session.getRoomsApiClient().initialSync(roomId, new SimpleApiCallback<RoomResponse>(){
            @Override
            public void onSuccess(RoomResponse info) {
                super.onSuccess(info);
                for (Event e: info.messages.chunk) {
                    if (e.type.equals(Event.EVENT_TYPE_MESSAGE)) {
                        ChatMessage msg;
                        String content = e.getContentAsJsonObject().get("body").getAsString();

                        String eventUser = session.getDataHandler().getUser(e.getSender()).displayname;
                        String matrixUser = session.getDataHandler().getUser(session.getDataHandler().getUserId()).displayname;

                        if (matrixUser.equals(eventUser))
                            msg = new ChatMessage(content, e.getAge(), ChatMessage.Type.SENT);
                        else
                            msg = new ChatMessage(content, e.getAge(), ChatMessage.Type.RECEIVED);

                        chatView.addMessage(msg);
                    }
                }
            }
        });

        // Listener waiting the send button to be pressed
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                session.getDataHandler().getRoom(roomId).sendTextMessage(chatMessage.getMessage(), null, null, null);
                return true;
            }
        });

    }
}
