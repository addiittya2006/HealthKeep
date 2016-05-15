package com.aaa.cybersrishti.model;

import java.util.Date;

/**
 * Created by addiittya on 15/05/16.
 */
public class FoodItem {
    int _id;
    String _name;
    String _cal_count;
    Date _date;

    public FoodItem(){}

    public FoodItem(int _id, String _name, String _cal_count, Date _date) {
        this._id = _id;
        this._name = _name;
        this._cal_count = _cal_count;
        this._date = _date;
    }

    public FoodItem(String _name, String _cal_count, Date _date) {
        this._name = _name;
        this._cal_count = _cal_count;
        this._date = _date;
    }

    public FoodItem(String _name, String _cal_count) {
        this._cal_count = _cal_count;
        this._name = _name;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_cal_count() {
        return _cal_count;
    }

    public void set_cal_count(String _cal_count) {
        this._cal_count = _cal_count;
    }

    public Date get_date() {
        return _date;
    }

    public void set_date(Date _date) {
        this._date = _date;
    }
}
