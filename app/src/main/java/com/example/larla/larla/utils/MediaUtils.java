package com.example.larla.larla.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MediaUtils {

    public static void clickMedia(Context context, MediaTypes type, String id) {
        File file = getFile(type, id);

        Log.d("MediaUtils", "clickMedia: " + file.getAbsolutePath());
        switch (type) {
            case AUDIO: {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case IMAGE:
            case VIDEO: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), type.toString().toLowerCase()+"/*");
                context.startActivity(intent);
                break;
            }

        }

    }

    private static File getFile(MediaTypes type, String id) {
        //return new File(type.getExternalPublicDirectory(), type.toString()+"1");
        return new File(type.getExternalPublicDirectory(), type.toString()+id.split(":")[0]+type.getExtension());
    }

    public static boolean isDowloaded(MediaTypes type, String id) {
        File file = getFile(type, id);
        return file.exists();
    }

    public static void downloadMedia(Context context, String downloadableUrl, final MediaTypes type, final String id) {
        downloadMedia(context,downloadableUrl,type,id,false);
    }


    public static void downloadMedia(Context context, String downloadableUrl, final MediaTypes type, final String id, boolean click) {

        if (click) {
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                        clickMedia(context,type,id);
                    }

                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        if (!isDowloaded(type, id)) {
            File file = getFile(type, id);
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadableUrl));
            request.setDestinationUri(Uri.fromFile(file));
            request.setVisibleInDownloadsUi(false);
            manager.enqueue(request);
        }
    }

    public enum MediaTypes {
        AUDIO, IMAGE, VIDEO;

        String getExtension() {
            switch (this){
                case AUDIO:
                    return ".m4a";
                case IMAGE:
                    return ".jpg";
                case VIDEO:
                    return ".mp4";
                default:
                    return ".txt";
            }
        }

        File getExternalPublicDirectory() {
            switch (this){
                case AUDIO:
                    return Environment.getExternalStoragePublicDirectory("Larla/Audios");
                case IMAGE:
                    return Environment.getExternalStoragePublicDirectory("Larla/Images");
                case VIDEO:
                    return Environment.getExternalStoragePublicDirectory("Larla/Videos");
                default:
                    return Environment.getExternalStoragePublicDirectory("Larla");
            }
        }
    }
}
