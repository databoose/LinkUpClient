package com.data.linkup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity {
    EditText codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Log.d("onCreate_Lobby", "Ran onCreate()");
        Globals.setInLobby("LobbyActivity_onCreate", true);

        Main();
    }

    public void Main() {
        while(true) {
            try {Thread.sleep(10);} // for some reason, if we don't do an operation here, this loop does not run
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Globals.GotConnectCode == true) {
                Log.d("LobbyActivity_Main", "Setting ConnectCode to UI");
                TextView codeView = findViewById(R.id.codeView);
                codeView.setText(Globals.ConnectCode);
                Globals.setGotConnectCode("LobbyActivity_Main", false); // this resets the switch for next connection, do not remove
                break;
            }
            else if (Globals.GotConnectCode == false) {
                //System.out.println("GotConnectCode is false");
                continue;
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
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (Globals.ReceivingConnection == true && Globals.SenderName != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LobbyActivity.this);

                                builder.setTitle("Incoming connection");
                                builder.setMessage("User " + Globals.SenderName + " wants to know your location, accept?");

                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                        Globals.setReceivingConnection("LobbyActivity_Main()", false);
                    }
                }
            }
        });
        //at end
        Globals.setIsVerified("LobbyActivity_Main()", false);
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