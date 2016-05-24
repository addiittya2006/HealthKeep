package com.aaa.cybersrishti;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;

/**
 * Created by Anip on 5/24/2016.
 */
public class Setting extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {
    Button logout;
    GoogleApiClient mGoogleApiClient;
    boolean mSignInClicked;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        logout = (Button) findViewById(R.id.google_logout);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        logout.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          if (mGoogleApiClient.isConnected()) {
                                              Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                              mGoogleApiClient.disconnect();
                                              mGoogleApiClient.connect();
                                              // updateUI(false);
//                                              SharedPreferences
                                              prefs = getSharedPreferences("application_settings", 0);
                                              prefs.edit().clear().commit();
                                              Intent intent =new Intent(Setting.this,Splash.class);
                                              finishAffinity();
                                              startActivity(intent);
                                              finish();
                                              System.err.println("LOG OUT ^^^^^^^^^^^^^^^^^^^^ SUCESS");

                                          }
                                      }
                                  }
        );
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mSignInClicked = false;

        // updateUI(true);
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}