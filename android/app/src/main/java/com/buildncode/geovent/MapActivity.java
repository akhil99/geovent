package com.buildncode.geovent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.CameraProfile;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseUser;

import org.json.JSONObject;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;

import com.buildncode.geovent.Point;
import com.buildncode.geovent.Polygon2;
import com.buildncode.geovent.Line;
import com.buildncode.geovent.GeoFence;
import com.buildncode.geovent.CompoundFence;
import com.buildncode.geovent.PolyFence;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapClickListener, GoogleMap.OnMarkerClickListener {

    GoogleMap map;

    Firebase myFirebaseRef;

    LatLng tapCoords;
    ArrayList<LatLng>myPoints;

    GoogleApiClient mGoogleApiClient;
    ParseUser user;

    boolean mapMoved = false;
    boolean isCreatingFence = false;

    Marker createEventMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        myFirebaseRef = new Firebase("https://geovent.firebaseio.com/");
        user = ParseUser.getCurrentUser();

        Button newFence = (Button) findViewById(R.id.newFence);
        isCreatingFence=false;
        tapCoords = new LatLng(1,1);
        myPoints = new ArrayList<LatLng>();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        newFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCreatingFence){ //if it finished, draw the fence
                    drawFence();
                    isCreatingFence = !isCreatingFence;
                }
                else{ //otherwise, start creating a fence
                    clearMap();
                    isCreatingFence = !isCreatingFence;
                    startFenceCreation();
                }

            }

        });

    }

    private void finishActivity(){
        Intent map = new Intent(this, EventsActivity.class);
        startActivity(map);
        finish();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        System.out.println("latLng");
        if(isCreatingFence){
            tapCoords = latLng;
            Toast.makeText(this, "Tap a spot or tap the button again to complete the fence", Toast.LENGTH_SHORT).show();
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(getTapCoords())
                    .title("")
                    .snippet(""));
            myPoints.add(latLng);
        }
    }

    public LatLng getTapCoords(){
        return tapCoords;
    }

    public void startFenceCreation(){
        if(isCreatingFence) {
            System.out.println("made toast");
            Toast.makeText(this, "Tap points to create a polygonal fence!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Your fence has been created", Toast.LENGTH_LONG).show();
            drawFence();
        }
    }

    public void drawFence(){
        Log.d("haxor", "draw fence");
        PolygonOptions p = new PolygonOptions();
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for(LatLng l: myPoints){
            p.add(l);
            bounds.include(l);
        }
        p.fillColor(Color.argb(252, 89, 89, 94));
        p.visible(true);
        p.strokeColor(Color.argb(252, 89, 89, 94));
        p.strokeWidth(10);
        map.addPolygon(p);

        LatLng center = bounds.build().getCenter();

        createEventMarker = map.addMarker(new MarkerOptions()
                .position(center)
                .title("Create Event"));
        createEventMarker.showInfoWindow();

        System.out.println(myPoints);

    }

    public void clearMap(){
        map.clear();
        myPoints.clear();
        isCreatingFence=false;
        tapCoords = new LatLng(1,1);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void startLocationUpdates() {
        LocationRequest r = LocationRequest.create();
        r.setInterval(10000);
        r.setFastestInterval(1000);
        r.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, r, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("haxor", "location changed");
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        JSONObject data = new JSONObject();
        myFirebaseRef.child("users").child(user.getObjectId()).child("latitude").setValue(location.getLatitude());
        myFirebaseRef.child("users").child(user.getObjectId()).child("longitude").setValue(location.getLongitude());
        if(map != null && !mapMoved){
            map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mapMoved = true;
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("haxor", "connected");
        map.moveCamera(CameraUpdateFactory.zoomTo(17));
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void onDestroy(){
        super.onDestroy();
        if(mGoogleApiClient != null)mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("haxor", "marker click");
        if(marker.equals(createEventMarker)){
            Log.d("haxor", "getting event name");
            getEventName();
        }
        return true;
    }

    private void getEventName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Event Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if(name == ""){
                    dialog.cancel();
                    clearMap();
                }

                myFirebaseRef.child("fences").child(user.getObjectId()).setValue(myPoints);
                myFirebaseRef.child("fences").child(user.getObjectId()).child("name").setValue(name);
                myPoints.clear();
                Toast.makeText(MapActivity.this, "Your fence has been created", Toast.LENGTH_LONG).show();
                finishActivity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                clearMap();
            }
        });

        builder.show();
    }

}
