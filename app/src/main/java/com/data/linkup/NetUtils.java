package com.data.linkup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class NetUtils
{
    int ConnVerify(BufferedReader netin, PrintWriter netout, Socket sock)
    {
        try
        {
            netout.write("Ar4#8Pzw<&M00Nk");
            netout.flush();

            do {
                String readLine = netin.readLine();

                if (readLine.equals("4Ex{Y**y8wOh!T00"))  {
                    return 1;
                }
                else {
                    Log.e("ConnThread", "Did not get confirmation string from server, instead got this : " + readLine);
                    return 0;
                }
            }
            while (true);
        }
        catch (IOException e) { Log.d("Connection-Verification", "Caught IOException"); }

        return 0; // this should be literally impossible to reach but compiler complains so this is why it's here
    }
}
