package com.yelj.mediademon.drawimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.yelj.mediademon.R;

/**
 * Author: Alex.ylj
 * 2019-06-05 13:39 Wednesday
 * Description: 三种绘制图片的方式
 */
public class DrawImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_image);

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.my_icon);

        //imageView
        ImageView image = findViewById(R.id.image);
        image.setImageBitmap(bitmap);

        //surfaceView
        SurfaceView surfaceView = findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (null == holder) {
                    return;
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);

                Canvas canvas = holder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
}
