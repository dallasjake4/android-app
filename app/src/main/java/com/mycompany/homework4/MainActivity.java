package com.mycompany.homework4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks {

    private GoogleMap map;
    private static final String SERVER_KEY = "&key=AIzaSyAAp2zFbyZdk4cUDB9O0u_3nk2BODucxws";
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
    private static final String SAVED_LOCATIONS = "savedLocations";
    private Marker marker;
    private Set<String> savedLocations = new HashSet<String>();
    public static final String PREFS_NAME = "myPrefs";
    static SharedPreferences sp;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
        savedLocations = new HashSet<String>(sp.getStringSet(SAVED_LOCATIONS, new HashSet<String>()));

        Intent intent = getIntent();

        addLocationServicesApi();

        String location = intent.getStringExtra("savedLocation");
        if (location != null) {
            updateMap(location);
        }


    }

    //add location services api, allowing the app to connect to location services
    protected synchronized void addLocationServicesApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    //this executes once the app is connected to location services
    public void onConnected(Bundle connection) {
        //get the user's location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        //display the location on the map
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        map.clear();
        marker = map.addMarker(new MarkerOptions().position(latLng));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setVisibility(View.VISIBLE);
        mGoogleApiClient.disconnect();
    }

    public void locateMe(View v) {
        //connect to location services
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_saved_locations) {
            Intent intent = new Intent(this, com.mycompany.homework4.SavedLocations.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateMap(String location) {
        location = location.replace(" ", "+");
        String url = GEOCODING_URL + location + SERVER_KEY;
        new FindLocationTask().execute(url);
    }

    public void findLocation(View view) {
        EditText location = (EditText) findViewById(R.id.location);
        String locationText = location.getText().toString();

        updateMap(locationText);
    }

    public void saveLocation(View view) {
        if (marker.getTitle() == null) {
            //this happens when accessing a user's current location
            //could do another AsycTask to do reverse geocoding get the address and save the location
            return;
        }
        if (!savedLocations.contains(marker.getTitle())) {
            savedLocations.add(marker.getTitle());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
        sp.edit().putStringSet(SAVED_LOCATIONS, savedLocations).apply();
    }

    private class FindLocationTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {

            HttpGet request = new HttpGet(urls[0]);

            HttpClient client = new DefaultHttpClient();
            try {
                HttpResponse response = client.execute(request);
                String responseStr = EntityUtils.toString(response.getEntity());
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                InputStream is = new ByteArrayInputStream(responseStr.getBytes());
                Document doc = db.parse(is);

                NodeList nodeList = doc.getElementsByTagName("result");

                Element firstLocation = (Element) nodeList.item(0);
                NodeList locationTagList = firstLocation.getElementsByTagName("location");
                Element locationTag = (Element) locationTagList.item(0);

                NodeList latList = locationTag.getElementsByTagName("lat");
                Element lat = (Element) latList.item(0);
                String latStr = lat.getFirstChild().getTextContent();

                NodeList lngList = locationTag.getElementsByTagName("lng");
                Element lng = (Element) lngList.item(0);
                String lngStr = lng.getFirstChild().getTextContent();

                NodeList addressList = firstLocation.getElementsByTagName("formatted_address");
                Element formattedAddress = (Element) addressList.item(0);
                String formattedAddressStr = formattedAddress.getFirstChild().getTextContent();

                String[] result = {latStr, lngStr, formattedAddressStr};
                return result;


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String[] result) {

            LatLng latLng = new LatLng(Double.parseDouble(result[0]), Double.parseDouble(result[1]));
            map.clear();
            marker = map.addMarker(new MarkerOptions().position(latLng).title(result[2]));

            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            Button saveButton = (Button) findViewById(R.id.save_button);
            saveButton.setVisibility(View.VISIBLE);
        }
    }
}
