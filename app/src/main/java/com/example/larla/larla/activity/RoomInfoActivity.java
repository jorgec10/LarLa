package com.example.larla.larla.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.RoomMember;

public class RoomInfoActivity extends AppCompatActivity {

    private MXSession session;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);

        roomId = this.getIntent().getStringExtra("roomId");
        session = Matrix.getInstance(getApplicationContext()).getSession();

        RoomState roomState = session.getDataHandler().getRoom(roomId).getState();

        setTitle(roomState.getDisplayName(session.getMyUserId()));

        EditText newMemberEmail = (EditText) findViewById(R.id.newMemberEmail);
        final String email = newMemberEmail.getText().toString();
        Button addMemberButton = (Button) findViewById(R.id.buttonAddMember);
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix.getInstance(RoomInfoActivity.this).getSession().getDataHandler().getRoom(roomId).inviteByEmail(email, new SimpleApiCallback<Void>());
            }
        });

        ListView membersList = (ListView) findViewById(R.id.membersList);
        ArrayAdapter<RoomMember> members = new ArrayAdapter<RoomMember>(this, android.R.layout.simple_list_item_1) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                RoomMember member = getItem(position);

                if (convertView == null) {
                    convertView = LayoutInflater.from(RoomInfoActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                TextView memberName = convertView.findViewById(android.R.id.text1);
                memberName.setText(member.getName());


                return convertView;
            }
        };

        for (RoomMember roomMember : roomState.getMembers()) {
            if (!roomMember.getUserId().equals(session.getMyUserId())) {
                members.add(roomMember);
            }
        }
        membersList.setAdapter(members);


    }
}
