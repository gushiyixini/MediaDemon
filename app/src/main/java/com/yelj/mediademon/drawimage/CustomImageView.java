package com.yelj.mediademon.drawimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.yelj.mediademon.R;

/**
 * Author: Alex.ylj
 * 2019-06-05 14:09 Wednesday
 * Description: 自定义view的方式显示图片
 */
public class CustomImageView extends View {

    private Bitmap bitmap;
    private Paint paint;

    public CustomImageView(Context context) {
        super(context);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.my_icon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != bitmap) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }
}
