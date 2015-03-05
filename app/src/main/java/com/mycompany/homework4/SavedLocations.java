package com.mycompany.homework4;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;


public class SavedLocations extends ActionBarActivity {

    private String[] savedLocations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_locations);
        savedLocations = (String[]) getIntent().getStringArrayExtra("savedLocations");
        LayoutInflater inflater = getLayoutInflater();

        for(String marker: savedLocations) {
            TextView textView = (TextView) inflater.inflate(R.layout.saved_locations_list_item, null);
            textView.setText(marker);
           // textView.setTag(marker);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(v);
                }
            });

            LinearLayout list = (LinearLayout) findViewById(R.id.saved_locations_list);
            list.addView(textView);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_locations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void findLocation(View v){
        TextView textView = (TextView) v;
        String savedLocation = textView.getText().toString();

        Intent intent = new Intent(this, com.mycompany.homework4.MainActivity.class);
        intent.putExtra("savedLocation", savedLocation);
        startActivity(intent);

    }
}
