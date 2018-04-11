package adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.larla.larla.R;

public class ChatListViewAdapter extends ArrayAdapter {

    //to reference the Activity
    private final Activity context;

    private final Integer[] imageIDarray;
    private final String[] userArray;
    private final String[] infoArray;


    public ChatListViewAdapter(Activity context, String[] userArrayParam, String[] infoArrayParam, Integer[] imageIDArrayParam){

        super(context, R.layout.chat_listview_row , userArrayParam);

        this.context = context;
        this.imageIDarray = imageIDArrayParam;
        this.userArray = userArrayParam;
        this.infoArray = infoArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {

        View rowView = view;
        if (view == null) {
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.chat_listview_row, parent,false);

            //this code gets references to objects in the chat_listview_row.xml file
            TextView nameTextField = (TextView) rowView.findViewById(R.id.userTextView);
            TextView infoTextField = (TextView) rowView.findViewById(R.id.infoTextView);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.chatImageView);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(userArray[position]);
            infoTextField.setText(infoArray[position]);
            imageView.setImageResource(imageIDarray[position]);
        }

        return rowView;

    };


}
