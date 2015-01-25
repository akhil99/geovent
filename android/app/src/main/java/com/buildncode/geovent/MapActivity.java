package com.buildncode.geovent;

import android.location.Location;
import android.media.CameraProfile;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.firebase.client.Firebase;
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
import com.parse.ParseUser;

import org.json.JSONObject;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap map;
    Firebase myFirebaseRef;
    GoogleApiClient mGoogleApiClient;
    ParseUser user;

    boolean mapMoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        myFirebaseRef = new Firebase("https://geovent.firebaseio.com/");
        user = ParseUser.getCurrentUser();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
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
}
