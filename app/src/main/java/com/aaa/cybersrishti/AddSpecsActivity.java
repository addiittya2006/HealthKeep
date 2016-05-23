package com.aaa.cybersrishti;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.aaa.cybersrishti.helpers.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddSpecsActivity extends AppCompatActivity {

    private static GoogleApiClient mClient = null;
    DatabaseHelper db = new DatabaseHelper(this);
    EditText etheight;
    EditText etweight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addspecs);

        etheight = (EditText) findViewById(R.id.et_height);
        etweight = (EditText) findViewById(R.id.et_weight);

    }

    private void buildFitnessClient() {

        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("hell", "Connected!!!");
                                new AddSpecsActivity.AddDataTask().execute();

                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i("hell", "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i("hell", "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i("hell", "Google Play services connection failed. Cause: " +
                                result.toString());

                    }
                })
                .build();

    }

    private class AddDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
//            DataReadRequest readRequest = null;
            try {
//                readRequest = queryFitnessData();

                Calendar c = Calendar.getInstance();
                Date d = new Date();
                c.setTime(d);
                long endTime = c.getTimeInMillis();
                c.add(Calendar.HOUR_OF_DAY, -1);
                long startTime = c.getTimeInMillis();

                DataSource dataSourceHeight = new DataSource.Builder()
                        .setDataType(DataType.TYPE_HEIGHT)
                        .setType(DataSource.TYPE_RAW)
                        .setAppPackageName(getApplicationContext())
                        .build();

                DataSet dataSeth = DataSet.create(dataSourceHeight);

                DataPoint dataPointh = dataSeth.createDataPoint()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                dataPointh.setFloatValues();
                dataSeth.add(dataPointh);

                DataSource dataSourceWeight = new DataSource.Builder()
                        .setDataType(DataType.TYPE_HEIGHT)
                        .setType(DataSource.TYPE_RAW)
                        .setAppPackageName(getApplicationContext())
                        .build();

                DataSet dataSetw = DataSet.create(dataSourceWeight);

                DataPoint dataPointw = dataSetw.createDataPoint()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                dataPointw.setFloatValues();
                dataSetw.add(dataPointw);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.food, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();



        switch (id) {
            case R.id.action_go:

//                db.addFoodItem(new FoodItem(etheight.getText().toString(), etweight.getText().toString()));
                buildFitnessClient();

                finish();

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
