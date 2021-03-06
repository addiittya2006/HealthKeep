package com.aaa.cybersrishti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.aaa.cybersrishti.helpers.DatabaseHelper;
import com.aaa.cybersrishti.model.FoodItem;

public class AddfoodActivity extends AppCompatActivity {

    DatabaseHelper db = new DatabaseHelper(this);
    EditText etfood;
    EditText etcal;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addfood);

        etfood = (EditText) findViewById(R.id.food_name);
        etcal = (EditText) findViewById(R.id.calcount);

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

                db.addFoodItem(new FoodItem(etfood.getText().toString(), etcal.getText().toString()));
                i = getIntent();
                setResult(Activity.RESULT_OK, i);
                finish();

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
