package com.example.larla.larla.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;
import com.example.larla.larla.fragments.LarlaMessageListFragment;
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

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        LarlaMessageListFragment fragment = (LarlaMessageListFragment) fm.findFragmentByTag("FRAGMENT");

        if (fragment == null) {
            // this fragment displays messages and handles all message logic
            fragment = LarlaMessageListFragment.newInstance(session.getMyUserId(), roomId, org.matrix.androidsdk.R.layout.fragment_matrix_message_list_fragment);
            fm.beginTransaction().add(R.id.chat_view, fragment, "FRAGMENT").commit();
        }

        // User messages input
        MessageInput messageInput = findViewById(R.id.input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message message = new Message("1", input.toString(), userAuthor, new Date(System.currentTimeMillis()));
                session.getDataHandler().getRoom(roomId).sendTextMessage(message.getText(), null, null, null);
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