package com.data.linkup;

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

// TODO : add toast if user is timed out/banned, and if user has timed out the connection

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

            int ret = NetUtilsObj.SendAndWaitReply("Ar4#8Pzw<&M00Nk", "4Ex{Y**y8wOh!T00", netin, netout); // Verification
            if (ret == 1) {
                Globals.IsVerified = true;
            }
            else if (ret == 0) {
                Log.e("ConnThread", "Verification failed, closing socket and not proceeding...");
                Globals.IsVerified = false;
                sock.close();
                return;
            }

            NetUtilsObj.Send("ny3_"+ Globals.HwidString, netout); // Sending

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

        Main();
    }

    public void Main()
    {
        Globals.HwidString = Settings.Secure.getString(getContentResolver(), "android_id");
        ((TextView) findViewById(R.id.lblHwid)).setText(Globals.HwidString);
    }

    public void showToast(String ToastString) {
        Toast toast = Toast.makeText(this , ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -10);
        toast.show();
    }


    public void btnGo(View v) throws InterruptedException {
        Globals.IsVerified = false;

        Thread tconn = new Thread(new ConnTask());
        tconn.start(); // starting thread to handle connection for us, doesn't mess with UI thread

        long start = System.currentTimeMillis();
        long timeout = start + 6000; // timeout is 6 seconds

        do {
            if (Globals.IsVerified == true) {
                Globals.IsVerified = false;

                System.out.println("True");
                Intent intent = new Intent(this, LobbyActivity.class);
                startActivity(intent);
                break;
            }

            if(System.currentTimeMillis() >= timeout) {
                showToast("Connection timed out");
                break;
            }
        } while (true);
    }
}