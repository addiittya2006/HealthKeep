package com.aaa.cybersrishti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.fitness.data.Value;
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
    public static GoogleApiClient mClient = null;
    ProgressBar calProgress;
    private static Float total=null;
    String text_data = null;
    static Value totalcount;
    int pStatus = 0;
    private Handler handler = new Handler();
    Boolean limit=false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    SharedPreferences prefs;
    boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //fitApi = (Button) findViewById(R.id.fit);

        prefs = getSharedPreferences("application_settings", 0);
        
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddfoodActivity.class);
                MainActivity.this.startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        ImageView profile = (ImageView) header.findViewById(R.id.imageView);
        TextView name = (TextView)header.findViewById(R.id.name);
        if (!prefs.getString("pic", "").equals("")) {
            Picasso.with(this).load(prefs.getString("pic", "")).into(profile);
            name.setText(prefs.getString("name",""));
        }
//
//        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            buildFitnessClient();
            // notify user you are online

        }
        else {
            calProgress = (ProgressBar)findViewById(R.id.circularProgressbar);
            calProgress.setMax(prefs.getInt("total",0));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    final DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                    Log.i("hell", String.valueOf(db.getTodayCalorieCount()));
                    while (pStatus < prefs.getInt("consumed",0)) {
                        pStatus += 1;
                        if(pStatus+10> total.intValue()){
                            limit=true;
                            break;

                        }

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                calProgress.setProgress(pStatus);
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            // Just to display the progress slowly
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            // notify user you are not online
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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
        int id = item.getItemId();
//        TODO Add any items for bar menu if available

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_fitness) {

            // Main Activity which is home
        } else if (id == R.id.nav_settings) {
            Intent intent=new Intent(MainActivity.this,Setting.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Health Keep");
            String shareMessage="Hey i am using this app to keep myself fit";
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,shareMessage);
            startActivity(Intent.createChooser(shareIntent,"Sharing via"));
        } else if (id == R.id.nav_feedback) {
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/html");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "healthkeep@anip.xyz" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            Email.putExtra(Intent.EXTRA_TEXT, "Dear ...," + "");
            startActivity(Intent.createChooser(Email, "Send Feedback:"));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
            TextView cal_burned = (TextView) findViewById(R.id.cal_burned);
            cal_burned.setText("Calories burned : "+ String.valueOf(total));

            calProgress = (ProgressBar)findViewById(R.id.circularProgressbar);
            calProgress.setMax(total.intValue());
            TextView cal_consumed = (TextView)findViewById(R.id.cal_consumed);
            final DatabaseHelper db =new DatabaseHelper(MainActivity.this);
            Log.i("hell",String.valueOf(db.getTodayCalorieCount()));
            cal_consumed.setText("Calories consumed: "+ String.valueOf(db.getTodayCalorieCount()));
            prefs = getSharedPreferences("application_settings", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("total",total.intValue());
            editor.putInt("consumed",Integer.valueOf(db.getTodayCalorieCount()));
            editor.commit();



//             calProgress.setProgressDrawable(draw);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    Log.i("hell", String.valueOf(db.getTodayCalorieCount()));
                    while (pStatus < db.getTodayCalorieCount()) {
                        pStatus += 1;
                        if(pStatus+10> total.intValue()){
                            limit=true;
                            break;

                        }

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                calProgress.setProgress(pStatus);
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            // Just to display the progress slowly
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
             // Maximum Progress
            //fitApi.setVisibility(View.GONE);
            if(limit==true)
            Toast.makeText(MainActivity.this,"You have reached the fucking deadline",Toast.LENGTH_LONG).show();
        }
    }

    public static void printData(DataReadResult dataReadResult) {
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
        DateFormat dateFormat = DateFormat.getTimeInstance();
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

    public static DataReadRequest queryFitnessData() throws ParseException {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day =cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String startDateString = String.valueOf(year)+"-"+"0"+String.valueOf(month)+"-"+String.valueOf(day)+" "+"0"+"00"+":"+"00"+":"+"00";
        Date now = new Date();
        //Log.i("hell",startDate);
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        cal.setTime(now);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = df.parse(startDateString);
        long startTime = startDate.getTime();
//        Log.i("hell", String.valueOf(startTime));
//        Log.i("hell",df.format(String.valueOf(startDate)));

        long endTime = cal.getTimeInMillis();
        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
//        long startTime = cal.getTimeInMillis();


        Log.i("hell", "Range Start: " + dateFormat.format(startTime));
        Log.i("hell", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.AGGREGATE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]
        return readRequest;
    }

}
