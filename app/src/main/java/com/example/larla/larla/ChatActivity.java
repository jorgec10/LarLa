package com.example.larla.larla;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_test_activity);

        String user = this.getIntent().getStringExtra("user");
        String info = this.getIntent().getStringExtra("info");

        ChatView chatView = (ChatView) findViewById(R.id.chat_view);

        // Listener waiting the send button to be pressed
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){
                // ToDo implement the logic to send the message to Matrix
                // If we can send the message, we return true to update the ChatView
                if (/*chatMessage.send()*/ true)
                    return true;
                // else, we do not update the view
                else
                    return false;
            }
        });

    }
}
