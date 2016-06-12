package com.aaa.cybersrishti.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.aaa.cybersrishti.model.FoodItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by addiittya on 15/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    Date _date;
    private static final String DATABASE_NAME = "foodKeeper";
    public int today_calorie_count=0;
    private static final String TABLE_FOOD = "fooditems";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CAL_COUNT = "cal_count";
    private static final String KEY_DATESTAMP = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_CAL_COUNT + " TEXT,"
                + KEY_DATESTAMP + " DATE DEFAULT (date('now'))" + ")";
        db.execSQL(CREATE_FOOD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);

        onCreate(db);
    }

    public void addFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, foodItem.get_name());
        values.put(KEY_CAL_COUNT, foodItem.get_cal_count());
        values.put(KEY_DATESTAMP,sdf.format(date));
        db.insert(TABLE_FOOD, null, values);
        db.close();
    }

    FoodItem getFoodItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FOOD, new String[] { KEY_ID, KEY_NAME, KEY_CAL_COUNT }, KEY_ID + "=?", 
                new String[] { String.valueOf(id) }, null, null, null, null);
        
        if (cursor != null)
            cursor.moveToFirst();

        FoodItem item = null;
        try {
            item = new FoodItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(3)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return item;
    }

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> fooditemList = new ArrayList<FoodItem>();
        String selectQuery = "SELECT  * FROM " + TABLE_FOOD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FoodItem fooditem = new FoodItem();
                fooditem.set_id(Integer.parseInt(cursor.getString(0)));
                fooditem.set_name(cursor.getString(1));
                fooditem.set_cal_count(cursor.getString(2));
                try {
                    fooditem.set_date(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(3)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                fooditemList.add(fooditem);
            } while (cursor.moveToNext());
        }

        return fooditemList;
    }

    public List<FoodItem> getTodayFoodItems() {
        List<FoodItem> fooditemList = new ArrayList<FoodItem>();
        String selectQuery = "SELECT * FROM " + TABLE_FOOD + " WHERE " + KEY_DATESTAMP + " = \"" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FoodItem fooditem = new FoodItem();
                fooditem.set_id(Integer.parseInt(cursor.getString(0)));
                fooditem.set_name(cursor.getString(1));
                fooditem.set_cal_count(cursor.getString(2));
                Log.i("hell", String.valueOf(today_calorie_count));
                try {
                    fooditem.set_date(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(3)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                fooditemList.add(fooditem);
            } while (cursor.moveToNext());
        }

        return fooditemList;
    }
    public int getTodayCalorieCount() {
        String selectQuery = "SELECT * FROM " + TABLE_FOOD + " WHERE " + KEY_DATESTAMP + " = \"" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + "\"";
        today_calorie_count=0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                today_calorie_count +=Integer.valueOf(cursor.getString(2));
                Log.i("hell", String.valueOf(today_calorie_count));

            } while (cursor.moveToNext());
        }

        return today_calorie_count;
    }
    public int updateFoodItem(FoodItem fooditem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, fooditem.get_name());
        values.put(KEY_CAL_COUNT, fooditem.get_cal_count());

        return db.update(TABLE_FOOD, values, KEY_ID + " = ?",
                new String[] { String.valueOf(fooditem.get_id()) });
    }

    public void deleteFoodItem(FoodItem fooditem) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD, KEY_ID + " = ?",
                new String[] { String.valueOf(fooditem.get_id()) });
        db.close();
    }


    public int getFoodItemsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FOOD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }
    public void truncateTable(){
        String query="DELETE FROM  "+TABLE_FOOD;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    public int getTotalCalorieCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FOOD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int total=0;
        if (cursor.moveToFirst()) {
            do {
                FoodItem fooditem = new FoodItem();
                fooditem.set_id(Integer.parseInt(cursor.getString(0)));
                fooditem.set_name(cursor.getString(1));
                fooditem.set_cal_count(cursor.getString(2));
                try {
                    fooditem.set_date(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(3)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                total=total+Integer.parseInt(cursor.getString(2));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return total;
    }


}