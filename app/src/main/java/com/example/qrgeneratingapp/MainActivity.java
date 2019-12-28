package com.example.qrgeneratingapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ProgressBar pgsBar;
    private int i = 0;

    private Handler hdlr = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        pgsBar = findViewById(R.id.pBar);
//        ImageView img = findViewById(R.id.imageView);


//        i = pgsBar.getProgress();
        new Thread(new Runnable() {
            public void run() {
                while (i < 100) {
                    i += 1;
                    try {
                        // Sleep for 100 milliseconds to show the progress slowly.
                        Thread.sleep(10);
                        if(i==99){
                            boolean flag = hasActiveInternetConnection(MainActivity.this);
                            if(flag == true){
                                if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                                    Intent intent = new Intent(MainActivity.this,ManufacturerProfile.class);
                                    startActivity(intent);

                                }else {
                                    Intent intent = new Intent(MainActivity.this,LoginManufacturer.class);
                                    startActivity(intent);
                                }

                            }
                            else{
                                Intent intent = new Intent(MainActivity.this,NoNet.class);
                                startActivity(intent);
                            }

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e("LOG:", "Error checking internet connection", e);
            }
        } else {
            Log.d("LOG:", "No network available!");
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
