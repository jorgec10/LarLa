package com.example.larla.larla.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;
import com.example.larla.larla.models.Author;
import com.example.larla.larla.models.Message;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.sync.RoomResponse;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChatKitActivity extends AppCompatActivity {

    private MessagesList messagesList;
    protected MessagesListAdapter<Message> messagesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_kit_activity);

        // Get session, room and user info
        final String roomId = this.getIntent().getStringExtra("roomId");
        final MXSession session = Matrix.getInstance(getApplicationContext()).getSession();
        String matrixUserId = session.getDataHandler().getUserId();
        String matrixUser = session.getDataHandler().getUser(matrixUserId).displayname;
        final Author userAuthor = new Author(matrixUser, matrixUser, "avatar");

        // Start adapter
        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        messagesAdapter = new MessagesListAdapter<>(matrixUser, null);
        this.messagesList.setAdapter(messagesAdapter);

        // Get room messages
        session.getRoomsApiClient().initialSync(roomId, new SimpleApiCallback<RoomResponse>(){
            @Override
            public void onSuccess(RoomResponse info) {
                super.onSuccess(info);
                List<Message> messages = new LinkedList<>();
                for (Event e: info.messages.chunk) {
                    if (e.type.equals(Event.EVENT_TYPE_MESSAGE)) {
                        Message msg;
                        String content = e.getContentAsJsonObject().get("body").getAsString();
                        String eventUser = session.getDataHandler().getUser(e.getSender()).displayname;

                        Author eventAuthor = new Author(eventUser, eventUser, "avatar");
                        msg = new Message("1", content, eventAuthor, new Date(System.currentTimeMillis()));
                        messages.add(msg);
                    }
                }
                messagesAdapter.addToEnd(messages, true);
            }
        });

        // User messages input
        MessageInput messageInput = findViewById(R.id.input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message message = new Message("1", input.toString(), userAuthor, new Date(System.currentTimeMillis()));
                session.getDataHandler().getRoom(roomId).sendTextMessage(message.getText(), null, null, null);
                messagesAdapter.addToStart(message, true);
                return true;
            }
        });

        // Attachment listener
        messageInput.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {
                Toast.makeText(ChatKitActivity.this, "Media was attached", Toast.LENGTH_SHORT).show();
            }
        });

    }

}