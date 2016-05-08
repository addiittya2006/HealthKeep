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
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

/**
 * Created by Anip on 5/7/2016.
 */
public class Login extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
//    CallbackManager callbackManager;
    SharedPreferences prefs;
    Person person;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private static final int RC_SIGN_IN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
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
            // If the error resolution was not successful we should not resolve further.
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
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();

        // Show a message to the user that we are signing in.
        //mStatusTextView.setText(R.string.signing_in);
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
            final String firstName = person.getName().getGivenName();
            final String lastName = person.getName().getFamilyName();
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
            Intent intent=new Intent(Login.this,MainActivity.class);
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
                // Could not resolve the connection result, show the user an
                // error dialog.
//                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            //showSignedOutUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This ensures that if the user denies the permissions then uses Settings to re-enable
        // them, the app will start working.
        //buildFitnessClient();
    }
    public static DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i("hell", "Range Start: " + dateFormat.format(startTime));
        Log.i("hell", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }
}
