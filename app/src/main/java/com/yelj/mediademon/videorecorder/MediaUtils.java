package com.yelj.mediademon.videorecorder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.yelj.mediademon.utils.BitmapUtils;
import com.yelj.mediademon.utils.FileUtils;
import com.yelj.mediademon.videorecorder.widget.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * MediaUtils
 * Created by wanbo on 2017/1/18.
 */

public class MediaUtils implements SurfaceHolder.Callback {
    private static final String TAG = "MediaUtils";
    public static final int MEDIA_AUDIO = 0;
    public static final int MEDIA_VIDEO = 1;
    private Activity activity;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    private CamcorderProfile profile;
    private Camera mCamera;
    private AutoFitTextureView textureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private File mVideoFile;
    private File mPictureFile;
    private File mVideoCoverFile;
    private String mVideoPath;
    private String mPicturePath;
    private String mVideoCoverPath;

    private int previewWidth, previewHeight;
    private int recorderType;
    private boolean isRecording;
    private GestureDetector mDetector;
    private boolean isZoomIn = false;
    private int or = 90;
    private int orFront = 270;
    private int cameraPosition = 0;//1代表前置摄像头，0代表后置摄像头
    private boolean safeToTakePicture = false;

    public MediaUtils(Activity activity) {
        this.activity = activity;
    }

    public void setRecorderType(int type) {
        this.recorderType = type;
    }

    /**
     * 设置surfaceView
     */
    public void setSurfaceView(SurfaceView view) {
        this.mSurfaceView = view;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(previewWidth, previewHeight);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mDetector = new GestureDetector(activity, new ZoomGestureListener());
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    //    public void setTextureView(AutoFitTextureView view) {
    //        this.textureView = view;
    //        initCamera();
    //        mDetector = new GestureDetector(activity, new ZoomGestureListener());
    //        this.textureView.setOnTouchListener(new View.OnTouchListener() {
    //            @Override
    //            public boolean onTouch(View v, MotionEvent event) {
    //                mDetector.onTouchEvent(event);
    //                return true;
    //            }
    //        });
    //    }

    /**
     * 设置视频保存地址
     */
    public void setVideoPath(String path) {
        this.mVideoPath = path;

    }

    /**
     * 设置照片保存地址
     */
    public void setPicturePath(String path) {
        this.mPicturePath = path;
    }

    /**
     * 设置视频封面保存地址
     */
    public void setVideoCoverPath(String path) {
        this.mVideoCoverPath = path;
    }

    /**
     * 获取视频地址
     */
    public String getVideoFilePath() {
        if (null != mVideoFile && mVideoFile.exists()) {
            return mVideoFile.getPath();
        } else {
            return "";
        }
    }

    /**
     * 获取照片地址
     */
    public String getPictureFilePath() {
        if (null != mPictureFile && mPictureFile.exists()) {
            return mPictureFile.getPath();
        } else {
            return "";
        }
    }

    /**
     * 获取视频封面地址
     */
    public String getVideoCoverPath() {
        if (null != mVideoCoverFile && mVideoCoverFile.exists()) {
            return mVideoCoverFile.getPath();
        } else {
            return "";
        }
    }

    /**
     * 获取视频时长
     */
    public int getVideoDuration() {
        if (null != mediaPlayer) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * 删除视频文件
     */
    private void deleteVideoFile() {
        if (null != mVideoFile && mVideoFile.exists()) {
            mVideoFile.delete();
        }
    }

    /**
     * 删除照片文件
     */
    private void deletePictureFile() {
        if (null != mPictureFile && mPictureFile.exists()) {
            mPictureFile.delete();
        }
    }

    /**
     * 删除视频封面文件
     */
    private void deleteVideoCoverFile() {
        if (null != mVideoCoverFile && mVideoCoverFile.exists()) {
            mVideoCoverFile.delete();
        }
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.startPreview();
        safeToTakePicture = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceView = null;
        mSurfaceHolder = null;
        if (mCamera != null) {
            Log.d(TAG, "surfaceDestroyed: ");
            releaseCamera();
        }
        if (mMediaRecorder != null) {
            releaseMediaRecorder();
        }
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }

    /**
     * 开始录制
     */
    public void record() {
        if (isRecording) {
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mVideoFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            isRecording = false;
        } else {
            startRecordThread();
        }
    }

    private boolean prepareRecord() {
        try {
            mMediaRecorder = new MediaRecorder();
            if (recorderType == MEDIA_VIDEO) {
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mMediaRecorder.setProfile(profile);
                if (cameraPosition == 0) {
                    mMediaRecorder.setOrientationHint(or);
                } else {
                    //前置摄像头输出视频的旋转角度为270
                    mMediaRecorder.setOrientationHint(orFront);
                }

            } else if (recorderType == MEDIA_AUDIO) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            mVideoFile = FileUtils.createNewFile(mVideoPath);
            mMediaRecorder.setOutputFile(mVideoFile.getPath());

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MediaRecorder", "Exception prepareRecord: ");
            releaseMediaRecorder();
            return false;
        }
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("MediaRecorder", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("MediaRecorder", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * 停止录制并保存
     */
    public void stopRecordSave() {
        Log.d("Recorder", "stopRecordSave");
        if (isRecording) {
            isRecording = false;
            try {
                mMediaRecorder.stop();
                saveVideoThumb(mVideoFile.getPath());
                Log.d("Recorder", mVideoFile.getPath());
            } catch (RuntimeException r) {
                Log.d("Recorder", "RuntimeException: stop() is called immediately after start()");
            } finally {
                releaseMediaRecorder();
                releaseCamera();
            }
        }
    }

    /**
     * 停止录制并删除文件
     */
    public void stopRecordUnSave() {
        Log.d("Recorder", "stopRecordUnSave");
        if (isRecording) {
            isRecording = false;
            try {
                mMediaRecorder.stop();
            } catch (RuntimeException r) {
                Log.d("Recorder", "RuntimeException: stop() is called immediately after start()");
            } finally {
                releaseMediaRecorder();
            }
        } else {
            //播放视频情况下
            releaseMediaPlayer();
            startPreView(mSurfaceHolder);
        }
        deleteVideoFile();
        deletePictureFile();
        deleteVideoCoverFile();
    }

    public void mStartPreview() {
        if (null != mCamera) {
            mCamera.startPreview();
        }
    }

    private void startPreView(SurfaceHolder holder) {
        if (mCamera == null) {
            //设置默认打开后置摄像头
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (mCamera != null) {
            if (cameraPosition == 0) {
                mCamera.setDisplayOrientation(or);
            } else {
                mCamera.setDisplayOrientation(or);
            }
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
                List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
                Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes, mSurfaceView.getWidth(), mSurfaceView.getHeight());

                if (null != optimalSize) {
                    // Use the same size for recording profile.
                    previewWidth = optimalSize.width;
                    previewHeight = optimalSize.height;
                    //设置摄像区域的大小
                    parameters.setPreviewSize(previewWidth, previewHeight);
                    //设置拍出来的屏幕大小
                    parameters.setPictureSize(previewWidth, previewHeight);
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                    // 分辨率越大视频大小越大，比特率越大视频越清晰
                    // 清晰度由比特率决定，视频尺寸和像素量由分辨率决定
                    // 比特率越高越清晰（前提是分辨率保持不变），分辨率越大视频尺寸越大。
                    profile.videoFrameWidth = optimalSize.width;
                    profile.videoFrameHeight = optimalSize.height;
                    profile.videoBitRate = 2 * optimalSize.width * optimalSize.height;
                }
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (null != focusModes) {
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                    } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    } else {
                        parameters.setFocusMode(focusModes.get(0));
                    }
                }
                //把上面的设置 赋给摄像头
                mCamera.setParameters(parameters);
                //把摄像头获得画面显示在SurfaceView控件里面
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            Log.d("Recorder", "release Recorder");
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
            Log.d("Recorder", "release Camera");
        }
    }

    private void startRecordThread() {
        if (prepareRecord()) {
            try {
                mMediaRecorder.start();
                isRecording = true;
                Log.d("Recorder", "Start Record");
            } catch (RuntimeException r) {
                releaseMediaRecorder();
                Log.d("Recorder", "RuntimeException: start() is called immediately after stop()");
            }
        }
    }

    private class ZoomGestureListener extends GestureDetector.SimpleOnGestureListener {
        //双击手势事件
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            super.onDoubleTap(e);
            Log.d(TAG, "onDoubleTap: 双击事件");
            if (!isZoomIn) {
                setZoom(20);
                isZoomIn = true;
            } else {
                setZoom(0);
                isZoomIn = false;
            }
            return true;
        }
    }

    private void setZoom(int zoomValue) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {
                int maxZoom = parameters.getMaxZoom();
                if (maxZoom == 0) {
                    return;
                }
                if (zoomValue > maxZoom) {
                    zoomValue = maxZoom;
                }
                parameters.setZoom(zoomValue);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 保存视频封面
     *
     * @param path 视频地址
     */
    private void saveVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        mVideoCoverFile = bitmap2File(media.getFrameAtTime(), mVideoCoverPath);
    }

    /**
     * bitmap转file
     */
    private File bitmap2File(Bitmap bitmap, String filePath) {
        File file = FileUtils.createNewFile(filePath);
        if (null != file) {
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 切换摄像头
     */
    public void changeCameraPosition() {
        //切换前后摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 0) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    releaseCamera();
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);//打开当前选中的摄像头
                    cameraPosition = 1;
                    startPreView(mSurfaceHolder);
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    releaseCamera();
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);//打开当前选中的摄像头
                    cameraPosition = 0;
                    startPreView(mSurfaceHolder);
                    break;
                }
            }
        }
    }

    /**
     * 播放视频
     */
    public void startMediaPlayer(String path) {
        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        if (null != mCamera && safeToTakePicture) {
            mCamera.takePicture(null, null, mPicture);
            safeToTakePicture = false;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        //对jpeg图像数据的回调,最重要的一个回调
        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.stopPreview();
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
            }
            //保存图片到sdcard
            if (null != b) {
                Bitmap rotaBitmap;
                if (cameraPosition == 0) {
                    rotaBitmap = BitmapUtils.getRotateBitmap(b, or);
                } else {
                    rotaBitmap = BitmapUtils.getRotateBitmap(b, orFront);
                }
                mPictureFile = bitmap2File(rotaBitmap, mPicturePath);
                deleteVideoFile();
            }
            safeToTakePicture = true;
        }
    };
}
