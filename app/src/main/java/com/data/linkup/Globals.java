package com.data.linkup;

import android.util.Log;

public class Globals {
    public static String HwidString;
    public static String ConnectCode;
    public static Boolean IsVerified;
    public static Boolean InLobby;
    public static Boolean GotConnectCode;

    public static void setHwidString(String callerIdent, String HWID) {
        HwidString = HWID;
        Log.d(callerIdent, "set HWID string to :" + HWID);
    }

    public static void setConnectCode(String callerIdent, String connectcode) {
        ConnectCode = connectcode;
        Log.d(callerIdent, "set ConnectCode to : " + connectcode);
    }

    public static void setIsVerified(String callerIdent, Boolean trueorfalse) {
        IsVerified = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set IsVerified to : " + trueorfalse);
        }
    }

    public static void setInLobby(String callerIdent, Boolean trueorfalse) {
        InLobby = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set InLobby to : " + trueorfalse);
        }
    }

    public static void setGotConnectCode(String callerIdent, Boolean trueorfalse) {
        GotConnectCode = trueorfalse;
        if(BuildConfig.DEBUG) {
            Log.d(callerIdent, "set GotConnectCode to : " + trueorfalse);
        }
    }
}
