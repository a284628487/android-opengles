package com.ccf.glesapp.mediaeffect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ccf.glesapp.R;

public class MediaEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaeffect);

        MediaEffectsFragment mFragment = new MediaEffectsFragment();
        getFragmentManager().beginTransaction().replace(R.id.content, mFragment).commitAllowingStateLoss();
    }
}
