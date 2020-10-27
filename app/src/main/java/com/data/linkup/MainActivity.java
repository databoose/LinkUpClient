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

// TODO : Get app to recognize when we enter activity_lobby and tell that to the server by sending a string "inlobby"

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

            long start = System.currentTimeMillis();
            long timeout = start + 6000; // timeout is 6 seconds

            CheckLobby:
            do {
                if (Globals.InLobby == true) {
                    Log.d("ConnThread", "Telling server we're in the lobby activity now");
                    NetUtilsObj.Send("inlobby", netout);
                    Globals.ConnectCode = netin.readLine();
                    Log.d("ConnThread", "ConnectCode : " + Globals.ConnectCode);
                    
                    break CheckLobby;
                }

                if(System.currentTimeMillis() >= timeout) {
                    Log.d("ConnThread", "Timed out on main do loop");
                    break CheckLobby;
                }
            } while (true);

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
    protected void onRestart() {
        super.onRestart();
        Globals.InLobby = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globals.InLobby = false;
        Globals.IsVerified = false;
        Main();
    }

    public void Main()
    {
        Globals.HwidString = Settings.Secure.getString(getContentResolver(), "android_id");
        ((TextView) findViewById(R.id.lblHwid)).setText(Globals.HwidString);
    }

    public void showToast(String ToastString) {
        Toast toast = Toast.makeText(this , ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }

    public void btnGo(View v) throws InterruptedException {
        Thread tconn = new Thread(new ConnTask());

        if(tconn.isAlive() == false) {
            tconn.start(); // starting thread to handle connection for us, doesn't mess with UI thread
        }
        else {
            return; // exits btnGo() early
        }

        long start = System.currentTimeMillis();
        long timeout = start + 6000; // timeout is 6 seconds

        do {
            if (Globals.IsVerified == true) {
                Globals.IsVerified = false;

                Log.d("btnGo","Verified");
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