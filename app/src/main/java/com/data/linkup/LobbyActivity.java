package com.data.linkup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Globals.InLobby = true;

        while(true) {
            if(Globals.ConnectCode != null) {
                TextView codeView = (TextView)findViewById(R.id.codeView);
                codeView.setText(Globals.ConnectCode);
                codeView.setGravity(Gravity.CENTER);
                break;
            }
        }
    }

    public void codeViewClick(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("test","test");
        clipboard.setPrimaryClip(clip);

        Toast toast = Toast.makeText(this , "Saved to clipboard", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 1200);
        toast.show();
    }
}