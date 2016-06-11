package com.aaa.cybersrishti.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
    int total=0;

    private ArrayList<FoodItem> mArrFood;
    private ListView lstView;
    private FoodFeedAdapter fa;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        View rootView=null;
        if(getArguments().getInt(ARG_SECTION_NUMBER)==1) {
            rootView = inflater.inflate(R.layout.home_tabbed, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        }


        else{
            rootView = inflater.inflate(R.layout.feed_tabbed,container,false);

            final DatabaseHelper db = new DatabaseHelper(getContext());

            mArrFood = new ArrayList<>();
            fa = new FoodFeedAdapter(mArrFood, rootView.getContext());
            fa.notifyDataSetChanged();
            lstView = (ListView) rootView.findViewById(R.id.listView);
            lstView.setDivider(null);
            lstView.setAdapter(fa);
            List<FoodItem> fooditems = db.getTodayFoodItems();

            for (FoodItem fitem : fooditems) {
                mArrFood.add(fitem);
//                        total=total+ Integer.parseInt(fitem.get_cal_count());
            }

            Collections.reverse(mArrFood);
            fa.notifyDataSetChanged();


            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mArrFood.clear();

                    swipeRefreshLayout.setRefreshing(true);
                    List<FoodItem> fooditems = db.getTodayFoodItems();

                    for (FoodItem fitem : fooditems) {
                        mArrFood.add(fitem);
//                        total=total+ Integer.parseInt(fitem.get_cal_count());
                    }
                    Collections.reverse(mArrFood);
                    fa.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        return rootView;

    }
}
