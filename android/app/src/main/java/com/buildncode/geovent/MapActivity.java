package com.buildncode.geovent;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapbox.mapboxsdk.views.MapView;
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


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapClickListener {

    MapView mapView;
    GoogleMap map;
    boolean isCreatingFence;
    LatLng tapCoords;
    ArrayList<LatLng>myPoints;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Button newFence = (Button) findViewById(R.id.newFence);
        isCreatingFence=false;
        tapCoords = new LatLng(1,1);
        myPoints = new ArrayList<LatLng>();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        newFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("ishere");
                isCreatingFence = !isCreatingFence;
                if(!isCreatingFence)
                    drawFence();
                startFenceCreation();
            }
        });

    }
    @Override
    public void onMapClick(LatLng latLng) {
        System.out.println("latLng");
        if(isCreatingFence){
            tapCoords = latLng;
            Toast.makeText(this, "Tap a spot or tap the button again to complete the fence", Toast.LENGTH_LONG).show();
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
    /*public ArrayList<Point> createFence() {

        while(isCreatingFence){

        }
        return myPoints;
    }*/

    public void drawFence(){
        Log.d("haxor", "draw fence");
        //Polygon polygon = Polygon2.genPolygon(polygon2, Color strokeColor, Color fillColor);
        //Polygon polygon = map.addPolygon(genOptions()
        /*Polygon polygon = map.addPolygon(new PolygonOptions()
                .add(new LatLng(37.48539817893045, -122.20416817814112))
                .add(new LatLng(37.48529016480885, -122.20204722136259))
                .add(new LatLng(37.48415520895677, -122.20178235322236))
                .add(new LatLng(37.48539817893045, -122.20416817814112))
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)
                .visible(true)
                .strokeWidth(50));
        map.addPolygon(new PolygonOptions()
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .add(new LatLng(21.291, -157.821))  // Hawaii
                .add(new LatLng(37.423, -122.091))  // Mountain View
                .add(new LatLng(-18.142, 178.431))  // Fiji
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .fillColor(Color.GRAY)
                .visible(true)
                .strokeColor(Color.GREEN)
                .strokeWidth(50));*/
        PolygonOptions p = new PolygonOptions();
        for(LatLng l: myPoints){
            p.add(l);
        }
        p.fillColor(Color.argb(252, 89, 89, 94));
        p.visible(true);
        p.strokeColor(Color.argb(252, 89, 89, 94));
        p.strokeWidth(10);
        map.addPolygon(p);




        //polygon.setPoints(Polygon2.getPolyPoints(polygon2));
        //Polygon polygon = map.addPolygon(new PolygonOptions()
                //.strokeColor(Color.RED)
                //.fillColor(Color.BLUE));
       // polygon.setPoints(Polygon2.getPolyPoints(polygon2));
        System.out.println(myPoints);


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
        r.setFastestInterval(5000);
        r.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, r, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        map = googleMap;
        map.setOnMapClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("haxor", "location changed");
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        System.out.print("Location: ");
        System.out.println(latlng);
        if(map != null)map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
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
}
