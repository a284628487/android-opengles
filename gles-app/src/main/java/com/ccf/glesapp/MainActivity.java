package com.ccf.glesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ccf.glesapp.polygon.Polygon3DActivity;
import com.ccf.glesapp.polygon.PolygonActivity;
import com.ccf.glesapp.texture.TextureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.base_shape:
                startActivity(new Intent(this, PolygonActivity.class));
                break;
            case R.id.threed_shape:
                startActivity(new Intent(this, Polygon3DActivity.class));
                break;
            case R.id.texture:
                startActivity(new Intent(this, TextureActivity.class));
                break;
        }
    }
}
