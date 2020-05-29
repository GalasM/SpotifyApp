package com.example.spotifyapp;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);
        String uri = "file:///android_asset/index.html";
        if(getIntent().hasExtra("help")){
            uri = uri +"#"+ getIntent().getStringExtra("help");
        }
        WebView myBrowser;
        myBrowser = findViewById(R.id.mybrowser);
        myBrowser.loadUrl(uri);
    }
}
