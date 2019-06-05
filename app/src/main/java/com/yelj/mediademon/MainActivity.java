package com.yelj.mediademon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.yelj.mediademon.drawimage.DrawImageActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDrawImage(View view) {
        startActivity(DrawImageActivity.class);
    }

    private void startActivity(Class cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        startActivity(intent);
    }
}
