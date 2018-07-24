package com.ccflying.glespolygon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ccflying.gles3dimensional.ThreeDActivity;
import com.ccflying.glescircle.CircleActivity;
import com.ccflying.glestriangle.TriangleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startActivity(new Intent(this, TriangleActivity.class));
//        startActivity(new Intent(this, CircleActivity.class));
        startActivity(new Intent(this, ThreeDActivity.class));
    }
}
