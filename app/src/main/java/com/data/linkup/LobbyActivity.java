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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Log.d("onCreate_Lobby", "Ran onCreate()");
        Globals.InLobby = true;

        Main();
    }

    public void Main()
    {
        while(true) {
            if(Globals.GotConnectCode == true) {
                Log.d("onCreate_Lobby", "Setting ConnectCode to UI");
                TextView codeView = (TextView)findViewById(R.id.codeView);
                codeView.setText(Globals.ConnectCode);
                codeView.setGravity(Gravity.CENTER);
                break;
            }
        }

        codeInput = (EditText) findViewById(R.id.editTextCode);
        codeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeInput.setCursorVisible(true);
            }
        });

        codeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus)
                    codeInput.setCursorVisible(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d("LobbyActivity","Back pressed in LobbyActivity, switching Globals.InLobby to false");
        Globals.InLobby = false;
        super.onBackPressed();
    }

    public void codeViewClick(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clip_label",Globals.ConnectCode);
        clipboard.setPrimaryClip(clip);

        Toast toast = Toast.makeText(this , "Saved to clipboard", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }
}