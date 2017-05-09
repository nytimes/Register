package com.nytimes.android.external.playbillingtester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.Switch;

import butterknife.BindView;

public class SampleActivity extends AppCompatActivity {

    @BindView(R.id.testerSwitch)
    Switch testerSwitch;

    @BindView(R.id.buySubButton)
    Button buySubButton;

    @BindView(R.id.buyIAPButton)
    Button buyIAPButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

}
