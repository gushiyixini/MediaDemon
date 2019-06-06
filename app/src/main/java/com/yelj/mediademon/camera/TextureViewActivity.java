package com.yelj.mediademon.camera;

import android.Manifest;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.yelj.mediademon.R;

import java.io.IOException;

/**
 * Author: Alex.ylj
 * 2019-06-06 09:52 Thursday
 * Description: camera采集视频数据
 */
public class TextureViewActivity extends AppCompatActivity {

    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_view);
        ActivityCompat.requestPermissions(this, permissions, 1001);

        TextureView textureView = findViewById(R.id.texture_view);

        //打开摄像头并旋转90度
        final Camera camera = Camera.open();
        camera.setDisplayOrientation(90);

        //Android常用的YUV格式有两种，一个是NV21，一个是YV12，Android一般默认是NV21
        //配置数据回调格式
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);

        //监听预览的回调
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });

        //TextureView预览
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    camera.setPreviewTexture(surface);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                camera.release();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
