package com.aaa.cybersrishti.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
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
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            calProgress = (ProgressBar) rootView.findViewById(R.id.circularProgressbar);
            Log.i("no of caaaaaccchhhhheee", "onCreate: " + prefs.getInt("total", 0));

            total = prefs.getInt("total", 0);
            if (total != 0) {
                calProgress.setMax(total);
            }
//            else {
//            if (calProgress != null){
//                calProgress.setMax(1000);
//            }

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

