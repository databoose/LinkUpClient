package com.data.linkup;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LobbyActivity extends AppCompatActivity {
    EditText codeInput;
    LocationManager location_manager = null;
    LocationListener location_listener = null;

    final ExecutorService executor = Executors.newFixedThreadPool(5);
    String inputSaved;

    void showToast(String ToastString) {
        Looper.prepare();
        Toast toast = Toast.makeText(getApplicationContext(), ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }

    @Override
    public void onStop() {
        Log.d("LobbyActivity", "System called onStop(), telling ConnTask we're done with LobbyActivity");
        Globals.setInLobby("LobbyActivity_onStop()", false);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Log.d("onCreate_Lobby", "Ran onCreate()");
        Globals.setInLobby("LobbyActivity_onCreate", true);

        Main();
    }

    public void btnConnect(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LobbyActivity.this);

        builder.setMessage("Enter name (The recipient sees this)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        input.setText(prefs.getString("inputSaved", inputSaved));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LobbyActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                Log.d("btnConnect", "inputSaved = " + prefs.getString("inputSaved", inputSaved));
                editor.putString("inputSaved", input.getText().toString());
                editor.apply();

                Log.d("btnConnect_OK_onClick()", "OK clicked");

                Globals.setName("btnConnect", input.getText().toString());
                Globals.setTargetCode("btnConnect", codeInput.getText().toString());
                Globals.setConnecting("btnConnect", true);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void showAlertDialog() {
        // could not be displaying second time because of socket timeout, or android SDK/compiler can literally be bugged here, utilizing finish() workaround though
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("showAlertDialog()", "ran");
                AlertDialog.Builder alert = new AlertDialog.Builder(LobbyActivity.this);
                alert.setMessage("User " + Globals.SenderName + " wants to know your location, accept?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getLocation();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d("runnable", "hello from runnable");
                                long start = System.currentTimeMillis();
                                long timeout = start + 8000; // timeout is 8 seconds

                                PrintWriter netout = null;
                                try { netout = new PrintWriter(Globals.sock.getOutputStream()); } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                NetUtils.Send("YES", netout);
                                while (true) {
                                    if (!Objects.equals(Globals.LatLong, "null")) {
                                        Log.d("showAlertDialog()", "Location : " + Globals.LatLong);
                                        Globals.setLatLong("showAlertDialog()", "null");
                                        Globals.setThreadDone("showAlertDialog Runnable", true);

                                        return;
                                    }
                                    else {
                                        Log.d("showAlertDialog()", "waiting");
                                        try { Thread.sleep(2000); }
                                        catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (System.currentTimeMillis() >= timeout) {
                                        Log.d("showAlertDialog()", "Timed out on main do loop");
                                        Globals.setThreadDone("showAlertDialog Runnable", true);
                                        return;
                                    }
                                }
                            }
                        };
                        executor.submit(runnable);

                        Runnable runnableLooper = new Runnable() {
                            public void run() {
                                for(;;) {
                                    if (Globals.ThreadDone == true) {
                                        Globals.setThreadDone("showAlertDialog()", false);
                                        Log.d("showAlertDialog()", "Thread is done");

                                        finish();
                                        System.exit(0);

                                        break;
                                    }
                                }
                            }
                        };
                        executor.submit(runnableLooper);
                    }
                });

                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                PrintWriter netout = null;
                                try { netout = new PrintWriter(Globals.sock.getOutputStream()); } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                NetUtils.Send("NO", netout);
                            }
                        };
                        executor.submit(runnable);
                    }
                });

                Dialog builder = alert.create();
                builder.show();
            }
        });
    }

     void getLocation() {
        //System.out.println("getLocation() called");

        location_manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Globals.setLatLong("onLocationChanged()", location.getLatitude() + "," + location.getLongitude());
                location_manager.removeUpdates(location_listener);
            }

            public void onProviderDisabled(String s) {
                showToast("GPS is disabled, please enable");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showToast("Need GPS permissions");
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET
                }, 10);
                Log.d("GPS", "permission issues?");
            }
        }

        try {
            location_manager.requestLocationUpdates("gps", 50, 0, location_listener);
        }
        catch (SecurityException z) {
            showToast("Need GPS permissions, returning to lobby");
            Globals.setInLobby("LobbyActivity_onBackPressed()", false);
        }
    }


    public void Main() {
        while (true) {
            try { Thread.sleep(100); } // conserve cpu cycles
            catch (InterruptedException e) { e.printStackTrace(); }
            if (Globals.GotConnectCode == true) {
                Log.d("LobbyActivity_Main", "Setting ConnectCode to UI");
                TextView codeView = findViewById(R.id.codeView);
                codeView.setText(Globals.ConnectCode);
                Globals.setGotConnectCode("LobbyActivity_Main", false); // this resets the switch for next connection, do not remove
                break;
            }
            else if (Globals.GotConnectCode == false) {
                //System.out.println("GotConnectCode is false");
            }
        }

        codeInput = findViewById(R.id.editTextCode);
        codeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeInput.setCursorVisible(true);
            }
        });

        codeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    codeInput.setCursorVisible(false);
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                for(;;) {
                    try {  Thread.sleep(500); } // conserve cpu
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (Globals.ReceivingConnection == true) {
                        showAlertDialog();
                        Globals.setReceivingConnection("LobbyActivity_Main()", false);
                    }

                    if (Globals.CrossMessage.equals("invalid_code")) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                showToast("Invalid code");
                            }
                        };
                        runnable.run();
                        Globals.setCrossMessage("LobbyActivity_Main()", "");
                    }
                    else {
                        //System.out.println("not");
                    }
                }
            }
        };
        executor.submit(runnable);

        //at end
        Globals.setIsVerified("LobbyActivity_Main()",false);
    }


    @Override
    public void onBackPressed() {
        Log.d("LobbyActivity", "Back pressed in LobbyActivity, switching Globals.InLobby to false");
        Globals.setInLobby("LobbyActivity_onBackPressed()", false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void codeViewClick(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clip_label", Globals.ConnectCode);
        clipboard.setPrimaryClip(clip);

        Toast toast = Toast.makeText(this, "Saved to clipboard", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }
}