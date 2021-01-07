package com.data.linkup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        //Log.d("Main", "Main got here");
        while(true) {
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

        //at end
        Globals.setIsVerified("btnGo", false);
    }

    public void btnConnect(View view) {
        Globals.setTargetCode("btnConnect", codeInput.getText().toString());
        Globals.setConnecting("btnConnect", true);
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