package com.aaa.cybersrishti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;

import com.aaa.cybersrishti.helpers.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;

/**
 * Created by addiittya on 13/06/16.
 */

public class SettingsActivity extends PreferenceActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    private AppCompatDelegate mDelegate;
    boolean mSignInClicked;
    static GoogleApiClient mGoogleApiClient;
    static SharedPreferences prefs;
    static DatabaseHelper db;

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new LogoutFragment()).commit();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    public static class LogoutFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference preference = (Preference) findPreference("logout");
            db = new DatabaseHelper(getActivity());

            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (mGoogleApiClient.isConnected()) {
                        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                        DatabaseHelper db =new DatabaseHelper(getActivity());
                        prefs = getActivity().getSharedPreferences("application_settings", 0);
                        prefs.edit().clear().commit();
                        db.truncateTable();
                        Intent intent =new Intent(getActivity(),Splash.class);
                        getActivity().finishAffinity();
                        startActivity(intent);
                        getActivity().finish();
                        Log.i("logout", "onPreferenceClick: SUCCESS");
                    }
                    db.truncateTable();
                    return true;
                }
            });


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
