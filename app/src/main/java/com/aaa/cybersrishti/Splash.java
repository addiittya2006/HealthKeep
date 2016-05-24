package com.aaa.cybersrishti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Anip on 5/24/2016.
 */
public class Splash extends AppCompatActivity {
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(4000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    prefs = getSharedPreferences("application_settings", 0);
                    Intent intent;
                    if(prefs.getBoolean("logged_in",false)==true)
                        intent = new Intent(Splash.this,MainActivity.class);
                    else
                        intent = new Intent(Splash.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

