package com.example.qrgeneratingapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class NoNet extends AppCompatActivity {
    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_net);

        //Initializing Text to Speech
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });

        //Playing audio
        ttobj.speak("Sorry. No internet connection! Restart the app after connecting to internet.", TextToSpeech.QUEUE_FLUSH, null);

    }
}
