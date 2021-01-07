package com.data.linkup;

import android.util.Log;

import java.net.Socket;

public class Globals {
    // SETTERS FOR CLIENT INFO
    public static String HwidString;
    public static void setHwidString(String callerIdent, String HWID) {
        HwidString = HWID;
        Log.d(callerIdent, "set HWID string to :" + HWID);
    }

    public static String ConnectCode;
    public static void setConnectCode(String callerIdent, String connectcode) {
        ConnectCode = connectcode;
        Log.d(callerIdent, "set ConnectCode to : " + connectcode);
    }

    public static Boolean IsVerified;
    public static void setIsVerified(String callerIdent, Boolean trueorfalse) {
        IsVerified = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set IsVerified to : " + trueorfalse);
        }
    }

    public static Boolean InLobby;
    public static void setInLobby(String callerIdent, Boolean trueorfalse) {
        InLobby = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set InLobby to : " + trueorfalse);
        }
    }

    public static Boolean GotConnectCode;
    public static void setGotConnectCode(String callerIdent, Boolean trueorfalse) {
        GotConnectCode = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set GotConnectCode to : " + trueorfalse);
        }
    }

    public static Socket sock;
    public static void clearSocket(String callerIdent, Socket socket) {
        sock = null;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "nullified sock (socket)");
        }
    }
    public static void setSocket(String callerIdent, Socket socket) {
        sock = socket;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set sock (socket) to : " + sock.toString());
        }
    }

    // SETTERS FOR CONNECTING TO TARGET CONNECT CODE
    public static Boolean Connecting;
    public static void setConnecting(String callerIdent, Boolean trueorfalse) {
        Connecting = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set Connecting to : " + trueorfalse);
        }
    }

    public static String TargetCode;
    public static void setTargetCode(String callerIdent, String targetcode) {
        TargetCode = targetcode;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set TargetCodde to : " + targetcode);
        }
    }
}
