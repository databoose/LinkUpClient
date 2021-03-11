package com.data.linkup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

    class ListenTask implements Runnable {
        public void run() {
            System.out.println("ListenTask started");
            BufferedReader netin = null;
            try { netin = new BufferedReader(new InputStreamReader(Globals.sock.getInputStream())); }
            catch (IOException e) {
                Log.e("ListenTask", "Error assigning netin : ");
                e.printStackTrace();
            }

            while (true && Globals.InLobby == true) {
                String ServMessage = "";
                try { ServMessage = netin.readLine(); }
                catch (SocketTimeoutException z) {
                    //Log.d("ListenTask", "socket timeout on ListenTask");
                }
                catch (IOException ReadException) {
                    System.out.println(ReadException);
                }

                if (ServMessage != null && ServMessage.contains("acceptordeny_") == true) {
                    String SenderName = ServMessage.replace("acceptordeny_", "");
                    Globals.setSenderName("ListenTask", SenderName);
                    System.out.println(SenderName + " wants to connect");
                    Globals.setReceivingConnection("ListenTask", true);
                }

                else if (ServMessage != null && ServMessage.equals("invalid_code") == true) {
                    Log.d("ListenTask", "Invalid code entered");
                    Globals.setCrossMessage("ListenTask","invalid_code");
                }

                else {
                    //Log.d("ListenTask", "got unknown message : " + "-" + ServMessage + "-");
                }
            }
        }
    }

    class ConnTask implements Runnable {
        public void run() {
            try {
                Globals.setSocket("ConnTask", new Socket("10.0.0.224", 64912));
                Globals.sock.setSoTimeout(12000);

                BufferedReader netin = new BufferedReader(new InputStreamReader(Globals.sock.getInputStream()));
                PrintWriter netout = new PrintWriter(Globals.sock.getOutputStream());

                int ret = NetUtils.SendAndWaitReply("Ar4#8Pzw<&M00Nk", "4Ex{Y**y8wOh!T00", netin, netout); // Verification
                if (ret == 1) {
                    Globals.IsVerified = true;
                }
                else if (ret == 0) {
                    Log.e("ConnThread", "Verification failed, closing socket and not proceeding...");
                    Globals.IsVerified = false;
                    Globals.sock.close();
                    return;
                }

                Log.d("ConnThread", "Sending HWID to server...");
                NetUtils.Send("ny3_" + Globals.HwidString, netout); // Sending

                long start = System.currentTimeMillis();
                long timeout = start + 6000; // timeout is 6 seconds

                while (true) {
                    Thread.sleep(20);
                    if (Globals.InLobby == true) {
                        NetUtils.Send("inlobby", netout);
                        Globals.setConnectCode("ConnTask", netin.readLine());
                        Globals.setGotConnectCode("ConnTask", true); // this is turned to false after received by LobbyActivity

                        break;
                    }

                    if (System.currentTimeMillis() >= timeout) {
                        Log.d("ConnThread", "Timed out on main do loop");
                        break;
                    }
                }

                new Thread(new ListenTask()).start();
                while (true) {
                    if(Globals.Connecting == true) {
                        Log.d("ConnThread", "User wants to connect to someone");
                        NetUtils.Send("connectto_" + Globals.TargetCode, netout); // prefix for buffer is connectto_ so the server knows we're trying to connect to target code
                        NetUtils.Send(Globals.Name, netout);
                        Globals.setConnecting("ConnThread",false); // reset
                    }

                    if (Globals.InLobby == false) {
                        Log.d("ConnThread", "Ending connection because user exited lobby, telling server we're done");
                        NetUtils.Send("done", netout);
                        break; // break to end and close our socket
                    }
                }

                Log.d("ConnThread", "Closing socket now");
                Globals.sock.close();
                Thread.sleep(50); //50ms to mitigate spamming
                return;
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
