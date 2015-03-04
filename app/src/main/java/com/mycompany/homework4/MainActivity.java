package com.mycompany.homework4;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends ActionBarActivity {

    private GoogleMap map;
    private static final String SERVER_KEY = "&key=AIzaSyAAp2zFbyZdk4cUDB9O0u_3nk2BODucxws";
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/xml?address=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
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
        if (id == R.id.action_settings) {
            return true;
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

    private class FindLocationTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... urls) {

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
                LatLng latLng = new LatLng(Double.parseDouble(latStr), Double.parseDouble(lngStr));
                return latLng;

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
        protected void onPostExecute(LatLng latLong){
            map.clear();
            Marker marker = map.addMarker(new MarkerOptions().position(latLong));
            map.moveCamera(CameraUpdateFactory.newLatLng(latLong));
            map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
    }
}
