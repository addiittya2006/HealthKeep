package com.aaa.cybersrishti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
    private static GoogleApiClient mClient = null;
    private ProgressBar calProgress;
    private static Float total=null;
    int exceeded_calorie=0;
    static Value totalcount;
    ScrollView home_content;
    private String text_data = null;
    final DatabaseHelper db =new DatabaseHelper(MainActivity.this);
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
//        calProgress = (ProgressBar) findViewById(R.id.circularProgressbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

//        calProgress.setMax(1000);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AddfoodActivity.class);
//                    startActivity(intent);
                    startActivityForResult(intent, 99);
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

        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            buildFitnessClient();
        }
//        else {
//            setProgress();
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 99 && resultCode == RESULT_OK) {
            TextView no_consumption = (TextView) findViewById(R.id.none_consumed);
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
            no_consumption.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    public float getTotalCalories(){
        return total;
    }

    private void setProgress() {

        pStatus=0;
        calProgress = (ProgressBar) findViewById(R.id.circularProgressbar);
        Log.i("no of caaaaaccchhhhheee", "onCreate: " + prefs.getInt("total", 0));

        if (prefs.getInt("total", 0) != 0) {
            calProgress.setMax(prefs.getInt("total", 0));
        }else {
//            if (calProgress != null){
            calProgress.setMax(1000);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus < prefs.getInt("consumed",0)) {

                    pStatus += 5;
                    if(pStatus> total.intValue()){
                        limit=true;
                        break;
                    }

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            calProgress.setProgress(pStatus);
                        }
                    });

                    try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (exit) {
            finish();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_fitness) {
            // Main Activity which is home
        } else if (id == R.id.nav_settings) {
            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Health Keep");
            String shareMessage="Hey, I am using this app to keep myself fit.\nDownload Here:\nhttp://play.google.com/store/apps/details?id=" +getPackageName();
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Sharing via:"));
        } else if (id == R.id.nav_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setType("text/plain");
//            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "healthkeep@anip.xyz" });
//            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
//            Email.putExtra(Intent.EXTRA_TEXT, "Dear ...," + "");
            Uri uri = Uri.parse("mailto:healthkeep@anip.xyz?subject=Healthkeep%20Feedback&body=Sir%2C%0AI%20would%20like%20to%20provide%20feedback%20for%20your%20app.%0A");
            emailIntent.setData(uri);
            startActivity(Intent.createChooser(emailIntent, "Send Feedback:"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
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
            if(total == 0){
                home_content = (ScrollView) findViewById(R.id.home_content);
                RelativeLayout error_container = (RelativeLayout) findViewById(R.id.error_container);
                FloatingActionButton add_food = (FloatingActionButton) findViewById(R.id.fab);
                add_food.setVisibility(View.GONE);
                home_content.setVisibility(View.GONE);
                error_container.setVisibility(View.VISIBLE);
                Button _fit_api=(Button)findViewById(R.id.fit_app);
                _fit_api.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String appPackageName = "com.google.android.apps.fitness";// getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
            }
            else if(db.getTodayCalorieCount() >= total.intValue()){
                RelativeLayout warning_container=(RelativeLayout)findViewById(R.id.cal_warning);
                warning_container.setVisibility(View.VISIBLE);
                home_content=(ScrollView)findViewById(R.id.home_content);
                TextView cal_exceed=(TextView)findViewById(R.id.cal_exceed);
                Log.i("hello", String.valueOf(db.getTodayCalorieCount()));
                Log.i("hello", String.valueOf(total.intValue()));
                exceeded_calorie=db.getTodayCalorieCount()-total.intValue();
                cal_exceed.setText("You have reached the deadline for today...... \n\nExtra calories consumed Today: "+exceeded_calorie+" kcal");
                home_content.setVisibility(View.GONE);

            }
            else{
                assert cal_burned != null;
                cal_burned.setText("Calories burned : "+ String.valueOf(total.intValue()));
            }
            calProgress = (ProgressBar)findViewById(R.id.circularProgressbar);
            calProgress.setMax(total.intValue());
            TextView cal_consumed = (TextView)findViewById(R.id.cal_consumed);
            TextView percentage = (TextView)findViewById(R.id.per_remaining);
            Log.i("hell",String.valueOf(db.getTodayCalorieCount()));
            Double per =0.0;
            cal_consumed.setText("Calories consumed: "+ String.valueOf(db.getTodayCalorieCount()));
            if(total.intValue() >= db.getTodayCalorieCount())
            {
                per= 100.0*db.getTodayCalorieCount()/total.intValue();
                Log.i("hell", "hgfhgfhf"+String.valueOf(per));
            }

            percentage.setText(String.valueOf(per.intValue())+"% Consumed");
            prefs = getSharedPreferences("application_settings", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("total",total.intValue());
            editor.putInt("consumed",db.getTodayCalorieCount());
            editor.apply();



            new Thread(new Runnable() {

                @Override
                public void run() {

                    Log.i("hell", String.valueOf(db.getTodayCalorieCount()));
                    while (pStatus < db.getTodayCalorieCount() &&  pStatus<= total.intValue()) {

                        pStatus += 5;
                        if(pStatus==total.intValue()){
                            limit=true;
                        }

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                calProgress.setProgress(pStatus);
                            }
                        });

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
            Log.i("hell", String.valueOf(limit));

        }
    }

    private static void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
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
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

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

                }).enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                        Log.i("hell", "Google Play services connection failed. Cause: " + result.toString());

                    }
                }).build();

    }

}
