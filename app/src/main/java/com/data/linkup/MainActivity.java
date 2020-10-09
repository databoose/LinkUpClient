package com.data.linkup;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// ny3_ is precode to hwid string

class ConnTask implements Runnable
{
    public void run()
    {
        NetUtils NetUtilsObj = new NetUtils();

        try
        {
            Socket sock = new Socket("10.0.0.224", 64912);
            sock.setSoTimeout(12000);

            BufferedReader netin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter netout = new PrintWriter(sock.getOutputStream());

            NetUtilsObj.SendAndWaitReply("Ar4#8Pzw<&M00Nk", "4Ex{Y**y8wOh!T00", netin, netout); // Verification

            Log.d("ConnThread", "Closing socket now");
            sock.close();
            Thread.sleep(50); //50ms to mitigate spamming
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String HwidString = Settings.Secure.getString(getContentResolver(), "android_id");
        ((TextView) findViewById(R.id.lblHwid)).setText(HwidString);
    }

    public void btnGo(View v)
    {
        // Log.d("btnGo","Clicked");
        Thread tconn = new Thread(new ConnTask());
        tconn.start(); // starting thread to handle connection for us, doesn't mess with UI thread
    }
}