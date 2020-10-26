package com.data.linkup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class NetUtils
{
    int SendAndWaitReply(String BufferOut, String ExpectedReply, BufferedReader netin, PrintWriter netout)
    {
        try
        {
            netout.write(BufferOut);
            netout.flush();
            System.out.println("Sent outwards buffer" + " " + '\"' + BufferOut + '\"' + " " + "to server, waiting for response from server");

            if (netin.readLine().equals(ExpectedReply)) {
                System.out.println("Got expected string from server");
                return 1;
            }

            else {
                System.out.println("Receive failure, did not get expected string...");
                System.out.println("Got : " + netin.readLine());

                return 0;
            }
        }

        catch (IOException e) { e.printStackTrace();}
        catch (NullPointerException z) {
            System.out.println("Null pointer exception, most likely the server cut off while we were waiting on readLine()");
            //z.printStackTrace();
        }

        return 0; // if it manages to somehow reach this, it probably failed anyways
    }

    void Send(String BufferOut, PrintWriter netout) {
        netout.write(BufferOut);
        netout.flush();
        System.out.println("Sent outwards buffer" + '"' + BufferOut + '"' + "to server");
    }
}
