package com.yelj.mediademon.videorecorder.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.yelj.mediademon.R;
import com.yelj.mediademon.videorecorder.Utils;

/**
 * SendView
 * Created by wanbo on 2017/1/20.
 */

public class SendView extends RelativeLayout {

    public RelativeLayout backLayout, selectLayout;
    private int btnScrollDistance;
    private int screenWidth;

    public SendView(Context context) {
        super(context);
        init(context);
    }

    public SendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        screenWidth = Utils.getInstance(context).getWidthPixels();
        btnScrollDistance = screenWidth / 3;
        LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.video_record_widget_send, null, false);
        layout.setLayoutParams(params);
        backLayout = layout.findViewById(R.id.return_layout);
        selectLayout = layout.findViewById(R.id.select_layout);
        addView(layout);
        setVisibility(GONE);
    }

    public void startAnim() {
        if (getVisibility() == GONE) {
            setVisibility(VISIBLE);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ObjectAnimator.ofFloat(backLayout, "translationX", 0, -btnScrollDistance), ObjectAnimator.ofFloat(selectLayout, "translationX", 0, btnScrollDistance));
            set.setDuration(250).start();
        }
    }

    public void stopAnim() {
        if (getVisibility() == VISIBLE) {
            setVisibility(GONE);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ObjectAnimator.ofFloat(backLayout, "translationX", -btnScrollDistance, 0), ObjectAnimator.ofFloat(selectLayout, "translationX", btnScrollDistance, 0));
            set.setDuration(250).start();
        }
    }

}
