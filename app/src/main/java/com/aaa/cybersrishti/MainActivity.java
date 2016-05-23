package com.aaa.cybersrishti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aaa.cybersrishti.adapters.SectionsPagerAdapter;
import com.aaa.cybersrishti.helpers.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static GoogleApiClient mClient = null;
    private ProgressBar calProgress;
    private static Float total=null;
    private String text_data = null;
    private int pStatus = 0;
    private Handler handler = new Handler();
    private Boolean limit=false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private SharedPreferences prefs;
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        assert mViewPager != null;
        mViewPager.setAdapter(mSectionsPagerAdapter);


        prefs = getSharedPreferences("application_settings", 0);
        
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AddfoodActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            });
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        View header= navigationView != null ? navigationView.getHeaderView(0) : null;
        ImageView profile = (ImageView) (header != null ? header.findViewById(R.id.imageView) : null);
        TextView name = (TextView) (header != null ? header.findViewById(R.id.name) : null);
        if (!prefs.getString("pic", "").equals("")) {
            Picasso.with(this).load(prefs.getString("pic", "")).into(profile);
            assert name != null;
            name.setText(prefs.getString("name",""));
        }

        buildFitnessClient();

    }

    @Override
    protected void onResume() {
        super.onResume();
        buildFitnessClient();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();

//        TODO Add any items for bar menu if available

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_fitness) {
            // Main Activity which is home
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_feedback) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
    private class ReadingDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            DataReadRequest readRequest = null;
            try {
                readRequest = queryFitnessData();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            Log.i("hell",String.valueOf(dataReadResult));

            printData(dataReadResult);

            text_data = String.valueOf(dataReadResult);

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            TextView mtextdata = (TextView) findViewById(R.id.section_label);
            if(total == 0){

            } else{
                assert mtextdata != null;
                mtextdata.setText(String.valueOf(total));
            }
            calProgress = (ProgressBar)findViewById(R.id.circularProgressbar);
            calProgress.setMax(total.intValue());
            final DatabaseHelper db = new DatabaseHelper(MainActivity.this);


//             calProgress.setProgressDrawable(draw);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Log.i("hell", String.valueOf(db.getTotalCalorieCount()));
                    while (pStatus < db.getTotalCalorieCount()) {
                        pStatus += 1;
                        if(pStatus+10> total.intValue()){
                            limit=true;
                            break;

                        }

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                calProgress.setProgress(pStatus);
                            }
                        });

                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
             // Maximum Progress
            //fitApi.setVisibility(View.GONE);
            if(limit)
            Toast.makeText(MainActivity.this,"You have reached the fucking deadline",Toast.LENGTH_LONG).show();
        }
    }

    private static void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            //Log.i("hell",dataReadResult.getBuckets());
            Log.i("hell", "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i("hell", "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private static void dumpDataSet(DataSet dataSet) {
        total= Float.valueOf(0);
        for (DataPoint dp : dataSet.getDataPoints()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Log.i("hell", "Data point:");
            Log.i("hell", "\tType: " + dp.getDataType().getName());
            Log.i("hell", "\tStart: " + sdf.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i("hell", "\tEnd: " + sdf.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i("hell", "\tField: " + field.getName() + " Value: " + dp.getValue(field)+dp.describeContents());
                Log.i("hell", String.valueOf(dp.getValue(field)));
                total += Float.parseFloat(String.valueOf(dp.getValue(field)));
            }
        }
    }

    private void buildFitnessClient() {

        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("hell", "Connected!!!");
                                new ReadingDataTask().execute();

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

    private static DataReadRequest queryFitnessData() throws ParseException {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day =cal.get(Calendar.DAY_OF_MONTH);
        String startDateString = String.valueOf(year)+"-"+"0"+String.valueOf(month)+"-"+String.valueOf(day)+" "+"0"+"00"+":"+"00"+":"+"00";
        Date now = new Date();
        cal.setTime(now);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = df.parse(startDateString);
        long startTime = startDate.getTime();
        long endTime = cal.getTimeInMillis();
        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        Log.i("hell", "Range Start: " + dateFormat.format(startTime));
        Log.i("hell", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.AGGREGATE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;

    }
}
