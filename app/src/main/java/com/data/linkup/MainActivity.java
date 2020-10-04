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

class ConnTask implements Runnable
{
    public void run()
    {
        try {
            Socket sock = new Socket("10.0.0.224", 64912);
            sock.setSoTimeout(12000);

            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            pw.write("Ar4#8Pzw<&M00Nk");
            pw.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            while (true)
            {
                String readLine = in.readLine();

                if (readLine == null) {
                    sock.close();
                    break;
                }

                else if (readLine.equals("4Ex{Y**y8wOh!T00")) {
                    Log.i("ConnThread","Server has confirmed that it has gotten our verification string");
                }

                else {
                    Log.e("ConnThread", "Did not get confirmation string from server, instead got this : " + readLine);
                }
            }

            sock.close();
            Thread.sleep(50); //80ms to mitigate spamming
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
        Log.d("btnGo","Clicked");
        Thread tconn = new Thread(new ConnTask());
        tconn.start(); // starting thread to handle connection for us, doesn't mess with UI thread
    }
}