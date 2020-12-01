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

// TODO : Optimize XML layout, make a standard for alignment. android:layout_alignParentStart="true" / android:layout_alignParentBottom="true" seem to work well
// TODO : Fix client freeze on second connection attempt in single session

class ConnTask implements Runnable {
    public void run() {
        NetUtils NetUtilsObj = new NetUtils();

        try {
            Socket sock = new Socket("10.0.0.225", 64912);
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

            Log.d("ConnThread", "Sending HWID to server...");
            NetUtilsObj.Send("ny3_" + Globals.HwidString, netout); // Sending

            long start = System.currentTimeMillis();
            long timeout = start + 6000; // timeout is 6 seconds

            while (true) {
                Thread.sleep(20);
                if (Globals.InLobby == true) {
                    Log.d("ConnThread", "Telling server we're in the lobby activity now");
                    NetUtilsObj.Send("inlobby", netout);
                    Globals.setConnectCode("ConnTask", netin.readLine());
                    Globals.setGotConnectCode("ConnTask", true); // this is turned to false after received by LobbyActivity

                    break;
                }

                if (System.currentTimeMillis() >= timeout) {
                    Log.d("ConnThread", "Timed out on main do loop");
                    break;
                }
            }

            while (true) {
                if (Globals.InLobby == false) {
                    Log.d("ConnThread", "Ending connection because user exited lobby, telling server we're done");
                    NetUtilsObj.Send("done", netout);
                    break; // break to end and close our socket
                }

                if(Globals.Connecting == true) {
                    Log.d("ConnThread", "User wants to connect to someone");
                    NetUtilsObj.Send("connectto_" + Globals.TargetCode, netout); // prefix for buffer is connectto_ so the server knows we're trying to connect to target code
                    Globals.setConnecting("ConnThread",false); // reset
                }
            }

            Log.d("ConnThread", "Closing socket now");
            sock.close();
            Thread.sleep(50); //50ms to mitigate spamming
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

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
        Main();
    }

    @SuppressLint("HardwareIds")
    public void Main() {
        Globals.setHwidString("MainActivity_Main()", Settings.Secure.getString(getContentResolver(), "android_id"));
        ((TextView) findViewById(R.id.lblHwid)).setText(Globals.HwidString);
    }

    public void showToast(String ToastString) {
        Toast toast = Toast.makeText(this, ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }

    public void btnGo(View view) throws InterruptedException {
        Thread tconn = new Thread(new ConnTask());
        Thread.sleep(300);

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