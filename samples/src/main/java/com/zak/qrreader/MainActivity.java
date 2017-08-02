package com.zak.qrreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by zeeshan on 7/22/2017.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView attendent_login;
    TextView attendent_logout;
    TextView visitor_login;
    TextView visitor_logout;
    static String SCAN = "scan";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        attendent_login = (TextView) findViewById(R.id.attendent_login);
        attendent_logout = (TextView) findViewById(R.id.attendent_logout);
        visitor_login = (TextView) findViewById(R.id.visitor_login);
        visitor_logout = (TextView) findViewById(R.id.visitor_logout);
        attendent_login.setOnClickListener(this);
        attendent_logout.setOnClickListener(this);
        visitor_login.setOnClickListener(this);
        visitor_logout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), DecoderActivity.class);
        switch (v.getId()) {
            case R.id.attendent_login:
                intent.putExtra(SCAN, 1);
                startActivity(intent);
                break;
            case R.id.attendent_logout:
                intent.putExtra(SCAN, 2);
                startActivity(intent);
                break;
            case R.id.visitor_login:
                intent.putExtra(SCAN, 3);
                startActivity(intent);
                break;
            case R.id.visitor_logout:
                intent.putExtra(SCAN, 4);
                startActivity(intent);
                break;
        }
    }
}
