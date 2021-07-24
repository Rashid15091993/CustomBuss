package com.example.bussinfoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSIONS_FINE_LOCATION = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//Выходит диалоговое окно о разрешение на предоставление геолокации
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                {
                    try
                    {
                        sleep(3000);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        Intent welcomeIntent = new Intent(MainActivity.this, BussMapsActivity.class);
                        startActivity(welcomeIntent);

                    }

                }
            }
        };
        thread.start();


    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

}