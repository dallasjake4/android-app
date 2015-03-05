package com.mycompany.homework4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class SavedLocations extends ActionBarActivity {

    private Set<String> savedLocations = new HashSet<String>();
    public static final String PREFS_NAME = "myPrefs";
    private static final String SAVED_LOCATIONS = "savedLocations";
    static SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_locations);
        sp = getSharedPreferences(PREFS_NAME, 0);
        savedLocations = new HashSet<String>(sp.getStringSet(SAVED_LOCATIONS, new HashSet<String>()));

        //put the saved locations in an array so it can be sorted
        String[] savedLocationsArray = Arrays.copyOf(savedLocations.toArray(), savedLocations.toArray().length, String[].class);
        Arrays.sort(savedLocationsArray);

        LayoutInflater inflater = getLayoutInflater();

        for(String marker: savedLocationsArray) {
            //set up each saved location
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.saved_locations_list_item, null);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(v);
                }
            });

            TextView textView = (TextView) item.findViewById(R.id.savedLocationText);
            textView.setText(marker);

            //set up delete button
            Button delete = (Button) item.findViewById(R.id.delete_button);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSavedLocation(v);
                }
            });


            LinearLayout list = (LinearLayout) findViewById(R.id.saved_locations_list);
            list.addView(item);
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
        TextView textView = (TextView) v.findViewById(R.id.savedLocationText);
        String savedLocation = textView.getText().toString();

        Intent intent = new Intent(this, com.mycompany.homework4.MainActivity.class);
        intent.putExtra("savedLocation", savedLocation);
        startActivity(intent);

    }

    public void deleteSavedLocation(View v){
        //get the String that is being removed from savedLocations
        View savedLocationLayout = (View) v.getParent();
        TextView savedLocationName = (TextView) savedLocationLayout.findViewById(R.id.savedLocationText);
        String name = savedLocationName.getText().toString();

        //remove the location from the SharedPreferences
        savedLocations.remove(name);
        sp.edit().putStringSet(SAVED_LOCATIONS, savedLocations).apply();

        //remove the view containing the string and delete button
        ((ViewGroup)savedLocationLayout.getParent()).removeView(savedLocationLayout);
    }
}
