package com.data.linkup;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

public class LobbyActivity extends AppCompatActivity {
    EditText codeInput;

    public enum UIfunc {
        showToast,
        showAlert,
        getLocation
    }

    void showToast(String ToastString) {
        Toast toast = Toast.makeText(getApplicationContext(), ToastString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Log.d("onCreate_Lobby", "Ran onCreate()");
        Globals.setInLobby("LobbyActivity_onCreate", true);

        Main();
    }

    void inUI(final UIfunc func, final String parameter) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (func) {
                    case showToast:
                        showToast(parameter);
                        break;
                    case showAlert:
                        showAlertDialog();
                        break;
                    case getLocation:
                        getLocation();
                        break;
                }
            }
        });
    }

     void getLocation() {
        System.out.println("getLocation() called");
        // Get the location manager
        final LocationManager location_manager;
        final LocationListener location_listener;

        location_manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Globals.setLatLong("onLocationChanged()", location.getLatitude() + "," + location.getLongitude());
                return;
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
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
        }

        try {
            location_manager.requestLocationUpdates("gps", 50, 0, location_listener);
            location_manager.removeUpdates(location_listener);
        }
        catch (SecurityException z) {
            showToast("Need GPS permissions, returning to lobby");
            Globals.setInLobby("LobbyActivity_onBackPressed()", false);
        }
    }

    public void btnConnect(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enter name (The recipient sees this)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Incoming connection");
        builder.setMessage("User " + Globals.SenderName + " wants to know your location, accept?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter netout = null;
                        try { netout = new PrintWriter(Globals.sock.getOutputStream()); } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NetUtils.Send("YES", netout);
                        inUI(UIfunc.getLocation, "null"); // this starts a new thread pretty sure
                        while (true) {
                            if (Globals.LatLong != "null") { // TODO this is still null for some reason, fix me
                                Log.d("showAlertDialog()", "Location : " + Globals.LatLong);
                                Globals.setLatLong("showAlertDialog()", "null");
                                break;
                            }
                            else {
                                //Log.d("showAlertDialog()", "waiting");
                            }
                        }
                    }
                };
                AsyncTask.execute(runnable);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
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
                AsyncTask.execute(runnable);
                dialog.dismiss();
            }
        });
        if(!LobbyActivity.this.isFinishing()) {
            builder.show();
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
                while (true) {
                    try {  Thread.sleep(500); } // conserve cpu
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Globals.ReceivingConnection == true) {
                        inUI(UIfunc.showAlert, "null");
                        Globals.setReceivingConnection("LobbyActivity_Main()", false);
                    }
                    if (Globals.CrossMessage.equals("invalid_code")) {
                        inUI(UIfunc.showToast, "Invalid code entered");
                        Globals.setCrossMessage("LobbyActivity_Main()", "");
                    }
                    else {
                        //System.out.println("not");
                    }
                }
            }
        };
        AsyncTask.execute(runnable);

        //at end
        Globals.setIsVerified("LobbyActivity_Main()",false);
    }

    @Override
    public void onBackPressed() {
        Log.d("LobbyActivity", "Back pressed in LobbyActivity, switching Globals.InLobby to false");
        Globals.setInLobby("LobbyActivity_onBackPressed()", false);
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        Log.d("LobbyActivity", "System called onStop(), telling ConnTask we're done with LobbyActivity");
        Globals.setInLobby("LobbyActivity_onStop()", false);
        super.onStop();
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