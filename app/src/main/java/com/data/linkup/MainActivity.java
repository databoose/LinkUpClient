package com.data.linkup;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String HwidString = Settings.Secure.getString(getContentResolver(), "android_id");
        ((TextView) findViewById(R.id.lblHwid)).setText(HwidString);
    }
}