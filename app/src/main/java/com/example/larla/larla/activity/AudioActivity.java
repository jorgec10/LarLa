package com.example.larla.larla.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioActivity extends BaseActivity {
    private Button play, stop, record, upload;
    private MediaRecorder myAudioRecorder;
    private String outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        // Buttons
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        upload = (Button) findViewById(R.id.upload);

        // Disable stop and play buttons while not recording
        stop.setEnabled(false);
        play.setEnabled(false);
        upload.setEnabled(false);

        // Store the audio file
        outputFile = getExternalCacheDir().getAbsolutePath() + "/recording.m4a";

        // Define audio specs
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_WB);
        myAudioRecorder.setOutputFile(outputFile);

        // Recording actions
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                record.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        // Stop actions
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                upload.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
            }
        });

        // Play actions
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = Matrix.getInstance(getApplicationContext()).getSession().getMediasCache().saveMedia(new FileInputStream(new File(outputFile)), "recording.m4a", "audio/m4a");

                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.putExtra("filename", "recording.m4a");
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}