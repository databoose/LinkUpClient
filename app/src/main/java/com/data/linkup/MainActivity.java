package com.data.linkup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing
        Globals.setInLobby("onCreate_MainActivity", false);
        Globals.setIsVerified("onCreate_MainActivity", false);
        Globals.setGotConnectCode("onCreate_MainActivity", false);
        Globals.setConnecting("onCreate_MainActivity", false);
        Globals.setReceivingConnection("onCreate_MainActivity", false);

        Globals.setThreadDone("onCreate_MainActivity", false);
        Globals.setCrossMessage("onCreate_MainActivity", "");
        Globals.setLatLong("onCreate_MainActivity", "null");
        Main();
    }

    @SuppressLint("HardwareIds")
    public void Main() {
        if (Globals.BuildIdString == null || Globals.BuildIdString.length() == 0) {
            int BuildId = (int)(Math.random() * (9999 - 1000 + 1) + 100);
            Globals.setBuildIdString("MainActivity_Main()", String.valueOf(BuildId));
        }
        ((TextView) findViewById(R.id.lblBuildId)).setText(Globals.BuildIdString);

        Globals.setHwidString("Main()_MainActivity", Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID));

    }

    public void showToast(String ToastString) {
        Toast toast = Toast.makeText(getApplicationContext(), ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }

    public void btnGo(View view) throws InterruptedException {
        Globals.clearSocket("btnGo");
        if (Globals.sock != null) {
            showToast("Error, connection probably already open, restart app");
            return;
        }
        Thread tconn = new Thread(new ConnTask());
        Thread.sleep(200);

        if (tconn.isAlive() == false) {
            tconn.start(); // starting thread to handle connection for us, doesn't mess with UI thread
        } else {
            return; // exits btnGo() early
        }

        long start = System.currentTimeMillis();
        long timeout = start + 6000; // timeout is 6 seconds

        do {
            Thread.sleep(40);
            if (Globals.IsVerified == true) {
                Log.d("btnGo", "Verified");
                Log.d("btnGo", "Connnectcode : " + Globals.ConnectCode);
                Intent intent = new Intent(this, LobbyActivity.class);
                startActivity(intent);
                break;
            }

            if (System.currentTimeMillis() >= timeout) {
                showToast("Connection timed out");
                break;
            }
        } while (true);

    }
}