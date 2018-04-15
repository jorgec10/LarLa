package com.example.larla.larla.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.larla.larla.R;
import com.example.larla.larla.models.Chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatListViewAdapter extends ArrayAdapter<Chat> {

    public ChatListViewAdapter(Activity context, ArrayList<Chat> chats){

        super(context, R.layout.chat_listview_row , chats);
    }

    public View getView(int position, View view, ViewGroup parent) {

        Chat chat = getItem(position);

        View rowView = view;
        if (view == null) {
            LayoutInflater inflater=LayoutInflater.from(getContext());
            rowView=inflater.inflate(R.layout.chat_listview_row, parent,false);

            //this code gets references to objects in the chat_listview_row.xml file
            TextView nameTextField = (TextView) rowView.findViewById(R.id.userTextView);
            TextView infoTextField = (TextView) rowView.findViewById(R.id.infoTextView);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.chatImageView);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(chat.getName());
            infoTextField.setText(chat.getInfo());
            imageView.setImageResource(chat.getImage());
        }

        return rowView;

    };
}
