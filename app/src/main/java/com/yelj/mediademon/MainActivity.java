package com.yelj.mediademon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yelj.mediademon.audio.AudioRecordActivity;
import com.yelj.mediademon.camera.SurfaceViewActivity;
import com.yelj.mediademon.camera.TextureViewActivity;
import com.yelj.mediademon.drawimage.DrawImageActivity;
import com.yelj.mediademon.videorecorder.activity.VideoRecorderActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDrawImage(View view) {
        startActivity(DrawImageActivity.class);
    }

    public void onAudioDemo(View view) {
        startActivity(AudioRecordActivity.class);
    }

    public void onPreviewBySurfaceView(View view) {
        startActivity(SurfaceViewActivity.class);
    }

    public void onPreviewByTextureView(View view) {
        startActivity(TextureViewActivity.class);
    }

    public void onVideoRecord(View view) {
        Intent in = new Intent();
        in.setClass(this, VideoRecorderActivity.class);
        startActivity(in);
        overridePendingTransition(R.anim.slide_in_from_bottom, 0);
    }

    private void startActivity(Class cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        startActivity(intent);
    }
}
