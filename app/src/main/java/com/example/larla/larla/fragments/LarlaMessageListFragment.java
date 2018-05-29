package com.example.larla.larla.fragments;

import android.os.Bundle;
import android.util.Log;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.adapters.LarlaMessagesAdapter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.db.MXMediasCache;
import org.matrix.androidsdk.fragments.MatrixMessageListFragment;

import java.util.List;

public class LarlaMessageListFragment extends MatrixMessageListFragment {

    public static LarlaMessageListFragment newInstance(String matrixId, String roomId, int layoutResId) {
        LarlaMessageListFragment f = new LarlaMessageListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putInt(ARG_LAYOUT_ID, layoutResId);
        args.putString(ARG_MATRIX_ID, matrixId);
        f.setArguments(args);
        return f;
    }

    @Override
    public MXSession getSession(String matrixId) {
        return Matrix.getInstance(getActivity()).getSession();
    }

    @Override
    public MXMediasCache getMXMediasCache() {
        return getSession().getMediasCache();
    }

    @Override
    public AbstractMessagesAdapter createMessagesAdapter() {

        return new LarlaMessagesAdapter(getSession(), getContext());
    }

}
