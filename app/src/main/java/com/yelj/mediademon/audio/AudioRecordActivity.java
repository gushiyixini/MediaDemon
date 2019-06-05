package com.yelj.mediademon.audio;

import android.media.AudioRecord;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yelj.mediademon.R;

/**
 * Author: Alex.ylj
 * 2019-06-05 14:19 Wednesday
 * Description:
 */
public class AudioRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
    }

    private void initAudioRecord(){
//        AudioRecord.getMinBufferSize()
    }

    public void startRecord(View view) {

    }
}
