package com.ccf.glesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ccf.glesapp.polygon.Polygon3DActivity;
import com.ccf.glesapp.polygon.PolygonActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, Polygon3DActivity.class));
    }
}
