package com.aaa.cybersrishti.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaa.cybersrishti.R;
import com.aaa.cybersrishti.model.FoodItem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by addiittya on 16/05/16.
 */
public class FoodFeedAdapter extends BaseAdapter {

    private LayoutInflater lf;

    ArrayList<FoodItem> mArrFoodItem = new ArrayList<>();

    public FoodFeedAdapter(ArrayList arr, Context c) {
        this.mArrFoodItem = arr;
        lf = LayoutInflater.from(c);
    }

    class  ViewHolder {
        TextView tvName;
        TextView tvCalories;
    }

    @Override
    public int getCount() {
        return mArrFoodItem.size();
    }

    @Override
    public Object getItem(int i) {
        return mArrFoodItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh ;
        if(view == null){
            vh = new ViewHolder();
            view = lf.inflate(R.layout.item_food,null);
            vh.tvName = (TextView) view.findViewById(R.id.txtName);
            vh.tvCalories = (TextView) view.findViewById(R.id.txtCalories);
            view.setTag(vh);
        }
        else{
            vh = (ViewHolder) view.getTag();
        }

        FoodItem item = mArrFoodItem.get(i);
        vh.tvName.setText(item.get_name());
        vh.tvCalories.setText(item.get_cal_count());
        return view;
    }

}
