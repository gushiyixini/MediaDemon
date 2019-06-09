package com.yelj.mediademon.videorecorder.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.yelj.mediademon.R;

/**
 * VideoProgressBar
 * Created by Wanbo on 2016/12/7.
 */

public class VideoProgressBar extends View {

    private static final String TAG = "VideoProgressBar";
    private boolean isCancel = false;
    private Paint mRecordPaint, mBgPaint;
    private RectF mRectF;
    private int progress;
    private OnProgressEndListener mOnProgressEndListener;
    private int width, height;
    private Context context;

    public VideoProgressBar(Context context) {
        super(context, null);
        init(context);
    }

    public VideoProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mRecordPaint = new Paint();
        mBgPaint = new Paint();
        mRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = this.getWidth();
        height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        // 设置画笔相关属性
        int mCircleLineStrokeWidth = 10;
        mRecordPaint.setAntiAlias(true);
        mRecordPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mRecordPaint.setStyle(Paint.Style.STROKE);
        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2 + .8f;
        mRectF.top = mCircleLineStrokeWidth / 2 + .8f;
        mRectF.right = width - mCircleLineStrokeWidth / 2 - 1.5f;
        mRectF.bottom = height - mCircleLineStrokeWidth / 2 - 1.5f;

        // 实心圆
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(Color.parseColor("#EFEFEF"));
        canvas.drawCircle(width / 2, width / 2, width / 2 - .5f, mBgPaint);
        // 绘制圆圈，进度条背景
        if (isCancel) {
            mRecordPaint.setColor(Color.parseColor("#00000000"));
            canvas.drawArc(mRectF, -90, 360, false, mRecordPaint);
            isCancel = false;
            return;
        }
        int mMaxProgress = 100;
        if (progress > 0 && progress < mMaxProgress) {
            mRecordPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
            if (progress == 99) {
                progress = 100;
            }
            canvas.drawArc(mRectF, -90, ((float) progress / mMaxProgress) * 360, false, mRecordPaint);
        } else if (progress == 0) {
            mRecordPaint.setColor(Color.parseColor("#00000000"));
            canvas.drawArc(mRectF, -90, 360, false, mRecordPaint);
        } else if (progress == mMaxProgress) {
            if (mOnProgressEndListener != null) {
                mOnProgressEndListener.onProgressEndListener();
            }
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }


    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
        invalidate();
    }


    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        mOnProgressEndListener = onProgressEndListener;
    }


    public interface OnProgressEndListener {
        void onProgressEndListener();
    }

}
