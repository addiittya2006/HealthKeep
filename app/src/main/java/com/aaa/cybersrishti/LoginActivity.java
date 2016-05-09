package com.aaa.cybersrishti;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

/**
 * Created by Anip on 5/7/2016.
 */
public class LoginActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    SharedPreferences prefs;
    Person person;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private static final int RC_SIGN_IN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .addScope(new Scope("email"))
                .build();
        Log.i("","entered login act");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            } else {

//                dialog.setTitle("Signing In");
//                dialog.setCancelable(false);
//                dialog.show();
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        } else {
//            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        googleLogin();

    }

    public void googleLogin(View v) {
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    public void googleLogin() {
        person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (person != null) {
            Log.i("hell", "fetching details");
            final String googleId = person.getId();
            final String name = person.getDisplayName();
            final String pic = person.getImage().getUrl();
            final String birthday = person.getBirthday() != null ? person.getBirthday() : "";
            Person.AgeRange ageRange = person.getAgeRange();
            final int ageRangeMin = ageRange != null ? ageRange.getMin() : 0;
            final int ageRangeMax = ageRange != null ? ageRange.getMax() : 0;
            final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            final String gender = (person.getGender() == Person.Gender.MALE) ? "male" : "female";
            prefs = getSharedPreferences("application_settings", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("fbId", googleId);
            editor.putString("name", name);
            editor.putString("pic", pic + "&sz=1000");
            editor.putString("email", email);
            editor.putString("birthday", birthday);
            editor.putString("ageRangeMin", String.valueOf(ageRangeMin));
            editor.putString("ageRangeMax", String.valueOf(ageRangeMax));
            editor.putString("gender", gender);
            editor.commit();
            Toast.makeText(this,name + pic + email,Toast.LENGTH_LONG).show();
            Toast.makeText(this,name + pic + email,Toast.LENGTH_LONG).show();
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);

        }
        else
        {
            Toast.makeText(this,"Error signining in. Please try again",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {

                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
//                showErrorDialog(connectionResult);
            }
        } else {
//            showSignedOutUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
