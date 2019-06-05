package com.yelj.mediademon.audio;

import android.Manifest;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yelj.mediademon.R;
import com.yelj.mediademon.global.PcmToWavUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.yelj.mediademon.global.GlobalConfig.AUDIO_FORMAT;
import static com.yelj.mediademon.global.GlobalConfig.CHANNEL_CONFIG;
import static com.yelj.mediademon.global.GlobalConfig.SAMPLE_RATE_IN_HZ;

/**
 * Author: Alex.ylj
 * 2019-06-05 14:19 Wednesday
 * Description:
 */
public class AudioRecordActivity extends AppCompatActivity {

    private static final String TAG = AudioRecordActivity.class.getSimpleName();

    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private AudioRecord audioRecord;
    private int recordBufSize;
    private boolean isRecording;

    private AudioTrack audioTrack;
//    private byte[] audioData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * @param view 按钮点击事件
     */
    public void onRecord(View view) {
        if (view instanceof Button) {
            Button recordBtn = (Button) view;
            String btnText = recordBtn.getText().toString();
            if (btnText.equals("开始录音")) {
                recordBtn.setText("停止录音");
                startRecord();
            } else {
                recordBtn.setText("开始录音");
                stopRecord();
            }
        }
    }

    /**
     * @param view 按钮点击事件
     */
    public void onConvert(View view) {
        convertPcm2Wav();
    }

    /**
     * @param view 按钮点击事件
     */
    public void play(View view) {
        if (view instanceof Button) {
            Button recordBtn = (Button) view;
            String btnText = recordBtn.getText().toString();
            if (btnText.equals("开始播放")) {
                recordBtn.setText("停止播放");
                play();
            } else {
                recordBtn.setText("开始播放");
                stoPlay();
            }
        }
    }

    /**
     * 开始录音
     */
    private void startRecord() {
        //获得最小bufferSize
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        //音频源、采样率、音频通道的配置、要返回音频数据的格式
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize);

        recordBufSize = minBufferSize;

        final File file = getMusicFile("AudioRecordTest.pcm");
        audioRecord.startRecording();
        isRecording = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                byte[] data = new byte[recordBufSize];
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != fos) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, recordBufSize);
                        //如果读取音频数据没有出现错误，就将数据写入到文件
                        if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                            try {
                                fos.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            isRecording = false;
        }
    }

    /**
     * 文件格式转换
     */
    private void convertPcm2Wav() {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        File pcmFile = getMusicFile("AudioRecordTest.pcm");
        File wavFile = getMusicFile("AudioRecordTest.wav");
        if (!wavFile.mkdirs()) {
            Log.e(TAG, "wavFile Directory not created");
        }
        if (wavFile.exists()) {
            wavFile.delete();
        }
        pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
    }

    /**
     * 播放
     */
    private void play() {
        playInModeStream();
    }

    /**
     * 停止
     */
    private void stoPlay() {
        if (audioTrack != null) {
            Log.d(TAG, "Stopping");
            audioTrack.stop();
            Log.d(TAG, "Releasing");
            audioTrack.release();
            Log.d(TAG, "Nulling");
        }
    }

    /**
     * 播放，使用stream模式
     */
    private void playInModeStream() {
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, channelConfig, AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_IN_HZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(channelConfig)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.play();

        File file = getMusicFile("AudioRecordTest.pcm");
        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 播放，使用static模式
//     */
//    private void playInModeStatic() {
//        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    InputStream in = getResources().openRawResource(R.raw.ding);
//                    try {
//                        ByteArrayOutputStream out = new ByteArrayOutputStream();
//                        for (int b; (b = in.read()) != -1; ) {
//                            out.write(b);
//                        }
//                        Log.d(TAG, "Got the data");
//                        audioData = out.toByteArray();
//                    } finally {
//                        in.close();
//                    }
//                } catch (IOException e) {
//                    Log.wtf(TAG, "Failed to read", e);
//                }
//                return null;
//            }
//
//
//            @Override
//            protected void onPostExecute(Void v) {
//                Log.i(TAG, "Creating track...audioData.length = " + audioData.length);
//
//                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
//                audioTrack = new AudioTrack(
//                        new AudioAttributes.Builder()
//                                .setUsage(AudioAttributes.USAGE_MEDIA)
//                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                .build(),
//                        new AudioFormat.Builder().setSampleRate(22050)
//                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
//                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
//                                .build(),
//                        audioData.length,
//                        AudioTrack.MODE_STATIC,
//                        AudioManager.AUDIO_SESSION_ID_GENERATE);
//                Log.d(TAG, "Writing audio data...");
//                audioTrack.write(audioData, 0, audioData.length);
//                Log.d(TAG, "Starting playback");
//                audioTrack.play();
//                Log.d(TAG, "Playing");
//            }
//
//        }.execute();
//    }

    private File getMusicFile(String fileName) {
        return new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName);
    }
}
