package com.yelj.mediademon.videorecorder.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yelj.mediademon.MainActivity;
import com.yelj.mediademon.R;
import com.yelj.mediademon.utils.FileUtils;
import com.yelj.mediademon.videorecorder.MediaUtils;
import com.yelj.mediademon.videorecorder.widget.SendView;
import com.yelj.mediademon.videorecorder.widget.VideoProgressBar;

/**
 * VideoRecorderActivity
 * Created by wanbo on 2017/1/18.
 */
public class VideoRecorderActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private SendView send;
    private TextView btn;
    private TextView tv_info;
    private RelativeLayout recordLayout;
    private VideoProgressBar progressBar;
    private RelativeLayout btn_change;
    private RelativeLayout btn_close;

    private int mProgress;
    private MediaUtils mediaUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_record_activity);
        initView();
    }

    private void initView() {
        surfaceView = findViewById(R.id.main_surface_view);
        send = findViewById(R.id.view_send);
        btn = findViewById(R.id.main_press_control);
        tv_info = findViewById(R.id.tv_info);
        recordLayout = findViewById(R.id.record_layout);
        progressBar = findViewById(R.id.main_progress_bar);
        btn_close = findViewById(R.id.btn_close);
        btn_change = findViewById(R.id.btn_change);

        btn_close.setOnClickListener(this);
        btn_change.setOnClickListener(this);

        // setting
        mediaUtils = new MediaUtils(this);
        mediaUtils.setRecorderType(MediaUtils.MEDIA_VIDEO);
        mediaUtils.setVideoPath(FileUtils.VEDIO_PATH_SNK + "video_" + System.currentTimeMillis() + FileUtils.VEDIO_FORMAT_MP4);
        mediaUtils.setPicturePath(FileUtils.IMG_PATH_PICTURE + "picture_" + System.currentTimeMillis() + FileUtils.IMG_FORMAT_JPG);
        mediaUtils.setVideoCoverPath(FileUtils.IMG_PATH_SNK_VEDIO_COVER + "cover_" + System.currentTimeMillis() + FileUtils.IMG_FORMAT_JPG);
        mediaUtils.setSurfaceView(surfaceView);

        btn.setOnTouchListener(btnTouch);

        send.backLayout.setOnClickListener(backClick);
        send.selectLayout.setOnClickListener(selectClick);
        // progress
        progressBar.setOnProgressEndListener(listener);
        progressBar.setCancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setCancel(true);
    }

    View.OnTouchListener btnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean ret = false;
            float downY = 0;
            int action = event.getAction();

            switch (v.getId()) {
                case R.id.main_press_control: {
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            mediaUtils.record();
                            //开始录制，隐藏提示文字和切换摄像头按钮
                            tv_info.setVisibility(View.GONE);
                            btn_change.setVisibility(View.GONE);
                            startView();
                            ret = true;
                            break;
                        case MotionEvent.ACTION_UP:
                            if (mProgress < 10) {
                                mediaUtils.takePhoto();
                                stopView(true);
                                break;
                            }
                            //停止录制
                            mediaUtils.stopRecordSave();
                            mediaUtils.startMediaPlayer(mediaUtils.getVideoFilePath());
                            stopView(true);
                            ret = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                }

            }
            return ret;
        }
    };

    VideoProgressBar.OnProgressEndListener listener = new VideoProgressBar.OnProgressEndListener() {
        @Override
        public void onProgressEndListener() {
            progressBar.setCancel(true);
            //停止录制
            mediaUtils.stopRecordSave();
            mediaUtils.startMediaPlayer(mediaUtils.getVideoFilePath());
            stopView(true);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressBar.setProgress(mProgress);
                    if (mediaUtils.isRecording()) {
                        mProgress = mProgress + 1;
                        sendMessageDelayed(handler.obtainMessage(0), 100);
                    }
                    break;
            }
        }
    };

    private void startView() {
        startAnim();
        mProgress = 0;
        handler.removeMessages(0);
        handler.sendMessage(handler.obtainMessage(0));
    }

    private void stopView(boolean isSave) {
        stopAnim();
        progressBar.setCancel(true);
        mProgress = 0;
        handler.removeMessages(0);
        if (isSave) {
            recordLayout.setVisibility(View.GONE);
            send.startAnim();
        }
    }

    private void startAnim() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(btn, "scaleX", 1, 0.7f), ObjectAnimator.ofFloat(btn, "scaleY", 1, 0.7f), ObjectAnimator.ofFloat(progressBar, "scaleX", 1, 1.5f), ObjectAnimator.ofFloat(progressBar, "scaleY", 1, 1.5f));
        set.setDuration(250).start();
    }

    private void stopAnim() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(btn, "scaleX", 0.7f, 1f), ObjectAnimator.ofFloat(btn, "scaleY", 0.7f, 1f), ObjectAnimator.ofFloat(progressBar, "scaleX", 1.5f, 1f), ObjectAnimator.ofFloat(progressBar, "scaleY", 1.5f, 1f));
        set.setDuration(250).start();
    }

    private View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send.stopAnim();
            //返回录制，显示提示文字和切换摄像头按钮
            tv_info.setVisibility(View.VISIBLE);
            btn_change.setVisibility(View.VISIBLE);
            recordLayout.setVisibility(View.VISIBLE);
            mediaUtils.stopRecordUnSave();
            mediaUtils.mStartPreview();
        }
    };

    private View.OnClickListener selectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send.stopAnim();
            recordLayout.setVisibility(View.VISIBLE);
            String videoPath = mediaUtils.getVideoFilePath();
            String videoCoverPath = mediaUtils.getVideoCoverPath();
            String picturePath = mediaUtils.getPictureFilePath();

            Intent intent = new Intent();
            intent.setClass(VideoRecorderActivity.this, MainActivity.class);
            if (!TextUtils.isEmpty(videoPath)) {
                //视频
                intent.putExtra("videoPath", videoPath);
                intent.putExtra("videoCoverPath", videoCoverPath);
                intent.putExtra("videoDuration", mediaUtils.getVideoDuration());
            } else if (!TextUtils.isEmpty(picturePath)) {
                //拍照
                intent.putExtra("cameraUrl", picturePath);
            }
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_to_bottom);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                //返回键
                finish();
                break;
            case R.id.btn_change:
                //切换摄像头
                mediaUtils.changeCameraPosition();
                break;
        }
    }
}
