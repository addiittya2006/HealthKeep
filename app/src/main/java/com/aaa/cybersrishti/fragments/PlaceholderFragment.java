package com.aaa.cybersrishti.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aaa.cybersrishti.R;
import com.aaa.cybersrishti.adapters.FoodFeedAdapter;
import com.aaa.cybersrishti.helpers.DatabaseHelper;
import com.aaa.cybersrishti.model.FoodItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by addiittya on 10/05/16.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    int total = 0;

    private ArrayList<FoodItem> mArrFood;
    private ListView lstView;
    private FoodFeedAdapter fa;
    ScrollView home_content;
    int exceeded_calorie=0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar calProgress;
    private SharedPreferences prefs;
    private int pStatus = 0;
    private Handler handler = new Handler();

    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences("application_settings", 0);
        View rootView = null;
        if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
            rootView = inflater.inflate(R.layout.home_tabbed, container, false);
            total = prefs.getInt("total", -1);
            DatabaseHelper db =new DatabaseHelper(getActivity().getApplicationContext());
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);

//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            if(total == 0){
                home_content = (ScrollView) rootView.findViewById(R.id.home_content);
                RelativeLayout error_container = (RelativeLayout) rootView.findViewById(R.id.error_container);
//                FloatingActionButton add_food = (FloatingActionButton) rootView.findViewById(R.id.fab);
//                add_food.setVisibility(View.GONE);
                home_content.setVisibility(View.GONE);
                error_container.setVisibility(View.VISIBLE);
                Button _fit_api=(Button)rootView.findViewById(R.id.fit_app);
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
            else if(db.getTodayCalorieCount() >= total && db.getTodayCalorieCount()!=0){
                RelativeLayout warning_container=(RelativeLayout) rootView.findViewById(R.id.cal_warning);
                warning_container.setVisibility(View.VISIBLE);
                home_content=(ScrollView)rootView.findViewById(R.id.home_content);
                TextView cal_exceed=(TextView)rootView.findViewById(R.id.cal_exceed);
                Log.i("hello", String.valueOf(db.getTodayCalorieCount()));
                Log.i("hello", String.valueOf(total));
                exceeded_calorie=db.getTodayCalorieCount()-total;
                cal_exceed.setText("You have reached the deadline for today...... \n\nExtra calories consumed Today: "+exceeded_calorie+" kcal");
                home_content.setVisibility(View.GONE);

            }
            calProgress = (ProgressBar) rootView.findViewById(R.id.circularProgressbar);
            Log.i("no of caaaaaccchhhhheee", "onCreate: " + prefs.getInt("total", 0));
            TextView cal_consumed = (TextView)rootView.findViewById(R.id.cal_consumed);
            TextView percentage = (TextView)rootView.findViewById(R.id.per_remaining);
            TextView cal_burned = (TextView) rootView.findViewById(R.id.cal_burned);
            Log.i("hell",String.valueOf(db.getTodayCalorieCount()));
            Double per =0.0;
            cal_consumed.setText("Calories consumed: "+ String.valueOf(db.getTodayCalorieCount()));
            cal_burned.setText("Calories burned : "+ String.valueOf(total));
            if(total >= db.getTodayCalorieCount())
            {
                per= 100.0*db.getTodayCalorieCount()/total;
                Log.i("hell", "hgfhgfhf"+String.valueOf(per));
            }

            percentage.setText(String.valueOf(per.intValue())+"% Consumed");
            if (total != 0) {
                calProgress.setMax(total);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (pStatus < prefs.getInt("consumed",0)) {

                        pStatus += 5;
                        if(pStatus > total){
//                            limit=true;
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


//            cal_burned.setText("Calories burned : "+ String.valueOf(total.intValue()));

        } else {
            rootView = inflater.inflate(R.layout.feed_tabbed, container, false);
            lstView = (ListView) rootView.findViewById(R.id.listView);
            lstView.setDivider(null);
            mArrFood = new ArrayList<>();
            final DatabaseHelper db = new DatabaseHelper(getContext());
            List<FoodItem> fooditems = db.getTodayFoodItems();
            fa = new FoodFeedAdapter(mArrFood, rootView.getContext());
            for (FoodItem fitem : fooditems) {
                mArrFood.add(fitem);
            }
            if(mArrFood.size()!=0) {
                lstView.setAdapter(fa);
            }
            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
            TextView no_consumption = (TextView) rootView.findViewById(R.id.none_consumed);
            Log.i("hrll", "nhjhjh" + String.valueOf(db.getTodayCalorieCount()));
            if (!(db.getTodayCalorieCount() > 0)) {
                Log.i("hekk", "entered uf");
                no_consumption.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
            } else {
                no_consumption.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                Collections.reverse(mArrFood);
                fa.notifyDataSetChanged();
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(true);
                        mArrFood.clear();

                        List<FoodItem> fooditems =  db.getTodayFoodItems();
                        for (FoodItem fitem : fooditems) {
                            mArrFood.add(fitem);
                        }

                        Collections.reverse(mArrFood);
                        fa.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
        return rootView;

    }
}

