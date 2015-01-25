package com.buildncode.geovent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseUser;

import org.json.JSONObject;

import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapClickListener, GoogleMap.OnMarkerClickListener {

    GoogleMap map;

    Firebase myFirebaseRef;

    LatLng tapCoords;
    LatLng lastPos;
    ArrayList<LatLng>myPoints;
    ArrayList<GeoFence> geofences;

    ArrayList<Marker> markers;
    Marker centerMarker;

    Polygon polygon;

    Map<Marker, String> fences;

    GoogleApiClient mGoogleApiClient;
    ParseUser user;

    boolean mapMoved = false;
    boolean isCreatingFence = false;


    Marker createEventMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fences = new HashMap<>();
        markers = new ArrayList<Marker>();

        myFirebaseRef = new Firebase("https://geovent.firebaseio.com/");
        user = ParseUser.getCurrentUser();
        loadFirebase();

        Button newFence = (Button) findViewById(R.id.newFence);
        isCreatingFence=false;
        tapCoords = new LatLng(1,1);
        myPoints = new ArrayList<LatLng>();
        geofences = new ArrayList<GeoFence> ();

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
    public void pushGeoFence(){
        geofences.add(createFence(myPoints));
    }


    private void loadFirebase(){
        myFirebaseRef.child("fences").addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase

            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                long cnt = snapshot.child("points").getChildrenCount();
                PolygonOptions polyOptions = new PolygonOptions();
                LatLngBounds.Builder bounds = LatLngBounds.builder();

                PolyFence pf = new PolyFence();

                for(int i = 0; i < cnt; i++){
                    Double lat = (Double)snapshot.child("points").child("" + i).child("latitude").getValue();
                    Double lon = (Double)snapshot.child("points").child("" + i).child("longitude").getValue();
                    LatLng ll = new LatLng(lat.doubleValue(), lon.doubleValue());
                    points.add(ll);
                    polyOptions.add(ll);
                    bounds.include(ll);
                    pf.addPoint(new Point(lat.doubleValue(), lon.doubleValue()));
                }

                Point curr = new Point(lastPos.latitude, lastPos.longitude);

                polyOptions.fillColor(Color.argb(150, 89, 89, 94));
                polyOptions.visible(true);
                polyOptions.strokeColor(Color.argb(252, 89, 89, 94));
                polyOptions.strokeWidth(10);
                map.addPolygon(polyOptions);
                if(pf.contains(curr)){
                    Log.d("haxors", "contains: " + snapshot.child("name").getValue(String.class));
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(bounds.build().getCenter())
                            .title(snapshot.child("name").getValue(String.class))
                            .snippet(""));
                    marker.showInfoWindow();

                    fences.put(marker, snapshot.getKey());
                }else{
                    Log.d("haxors", "doesnt contain: " + snapshot.child("name").getValue(String.class));
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setEvent(String id){
        Firebase ref = myFirebaseRef.child("users").child(ParseUser.getCurrentUser().getObjectId());
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("event", id);
        m.put("latitude", lastPos.latitude);
        m.put("longitude", lastPos.longitude);
        ref.setValue(m);
        Intent map = new Intent(this, EventsActivity.class);
        map.putExtra("eventId", id);
        startActivity(map);
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
            markers.add(marker);
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
        PolygonOptions polyOptions = new PolygonOptions();
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for(LatLng l: myPoints){
            polyOptions.add(l);
            bounds.include(l);
        }
        polyOptions.fillColor(Color.argb(150, 89, 89, 94));
        polyOptions.visible(true);
        polyOptions.strokeColor(Color.argb(252, 89, 89, 94));
        polyOptions.strokeWidth(10);
        polygon = map.addPolygon(polyOptions);
        LatLng center = bounds.build().getCenter();

        createEventMarker = map.addMarker(new MarkerOptions()
                .position(center)
                .title("Create Event"));
        createEventMarker.showInfoWindow();
        centerMarker = createEventMarker;

        for(Marker m:markers)m.remove();

        System.out.println(myPoints);

    }

    public void clearMap(){
        if(polygon != null)polygon.remove();
        myPoints.clear();
        for(Marker m:markers)m.remove();
        markers.clear();
        if(centerMarker != null){
            centerMarker.remove();
            centerMarker = null;
        }
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
        lastPos = latlng;
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
        Log.d("HAXORS", "ondestroy");
        super.onDestroy();
        if(mGoogleApiClient != null)mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("haxor", "marker click");
        if(marker.equals(createEventMarker)){
            Log.d("haxor", "getting event name");
            getEventName();
        }else{
            String eventId = fences.get(marker);
            if(eventId != null){
                Log.d("haxor", "event id: " + eventId);
                setEvent(eventId);
            }

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

                myFirebaseRef.child("fences").child(user.getObjectId()).child("points").setValue(myPoints);
                myFirebaseRef.child("fences").child(user.getObjectId()).child("name").setValue(name);
                myPoints.clear();
                Toast.makeText(MapActivity.this, "Your fence has been created", Toast.LENGTH_LONG).show();
                setEvent(ParseUser.getCurrentUser().getObjectId());
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
    public GeoFence createFence(ArrayList<LatLng> list){
        PolyFence fence = new PolyFence();
        for(LatLng i: list){
            fence.addPoint(new Point(i.longitude, i.latitude));
        }
        fence.seedPoints();
        return fence;
    }

}
