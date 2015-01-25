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

    LatLng lastPos;

    ArrayList<Event> events;
    Event buildEvent;

    public static class Event{

        LatLng center;
        ArrayList<LatLng> points;

        Marker centerMarker;
        ArrayList<Marker> markers;

        PolyFence pf;
        String name;
        String id;

        PolygonOptions polyOptions;

        GoogleMap map;

        public Event(String id, GoogleMap map){
            this(id, "test", map);
        }

        public Event(String id, String name, GoogleMap map){
            this.name = name;
            this.id = id;
            this.map = map;
            center = new LatLng(1,1);
            //latLngs = new ArrayList<>();
            pf = new PolyFence();
            polyOptions = new PolygonOptions();
            polyOptions.fillColor(Color.argb(150, 89, 89, 94));
            polyOptions.strokeColor(Color.argb(252, 89, 89, 94));
            polyOptions.strokeWidth(10);
            polyOptions.visible(true);
        }

        public void setCenter(LatLng c){ center = c; }

        public Marker getCenterMarker(){
            if(centerMarker != null)return centerMarker;
             centerMarker = map.addMarker(new MarkerOptions()
                    .position(center)
                    .title(name)
                    .snippet(""));
            centerMarker.showInfoWindow();
            return centerMarker;
        }

        public void removeCenterMarker(){
            if(centerMarker != null)centerMarker.remove();
            centerMarker = null;
        }

        public void addMarker(LatLng marker, boolean visible){
            //latLngs.add(marker);
            pf.addPoint(new Point(marker));
            polyOptions.add(marker);
            Marker m = map.addMarker(new MarkerOptions()
                    .position(marker)
                    .title("")
                    .visible(visible)
                    .snippet(""));
        }

        public void removeMarkers(){
            for(Marker m:markers)m.remove();
        }

        public String getName(){ return name; }

        public String getId(){ return id; }

        public PolygonOptions getPolyOptions(){ return polyOptions; }

        public PolyFence getPf(){ return pf; }

    }

    GoogleApiClient mGoogleApiClient;

    ParseUser user;

    boolean mapMoved = false;
    boolean isCreatingFence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        user = ParseUser.getCurrentUser();

        events = new ArrayList<Event>();

        Button newFence = (Button) findViewById(R.id.newFence);
        isCreatingFence=false;
        lastPos = new LatLng(1, 1);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        newFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCreatingFence){ //if it finished, draw the fence
                    drawFence();
                    isCreatingFence = false;
                }
                else{ //otherwise, start creating a fence
                    clearMap();
                    isCreatingFence = true;
                    startFenceCreation();
                }

            }

        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        map = googleMap;
        buildEvent = new Event(ParseUser.getCurrentUser().getObjectId(), map);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        buildGoogleApiClient();
        loadFirebase();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void loadFirebase(){
        myFirebaseRef = new Firebase("https://geovent.firebaseio.com/");
        myFirebaseRef.child("fences").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {

                Event e = new Event(snapshot.getKey(), snapshot.child("name").getValue(String.class), map);

                LatLngBounds.Builder bounds = LatLngBounds.builder();

                long cnt = snapshot.child("points").getChildrenCount();
                for(int i = 0; i < cnt; i++){
                    Double lat = (Double)snapshot.child("points").child("" + i).child("latitude").getValue();
                    Double lon = (Double)snapshot.child("points").child("" + i).child("longitude").getValue();
                    LatLng ll = new LatLng(lat.doubleValue(), lon.doubleValue());
                    e.addMarker(ll, false);
                    bounds.include(ll);
                }
                Point center = new Point(lastPos.latitude, lastPos.longitude);
                e.setCenter(bounds.build().getCenter());
                events.add(e);
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

    private void loadEvents(){
        for(Event e:events){
            PolyFence pf = e.getPf();
            Point center = new Point(lastPos);
            if(pf.contains(center)){ //if the event is within the user's range, make the center marker visible
                e.getCenterMarker();
                map.addPolygon(e.getPolyOptions());
            }else{
                e.removeCenterMarker();
            }
        }
    }


    private void setEvent(Event e){
        Firebase ref = myFirebaseRef.child("users").child(user.getObjectId());
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("event", e.getId());
        m.put("latitude", lastPos.latitude);
        m.put("longitude", lastPos.longitude);
        ref.setValue(m);
        Intent map = new Intent(this, EventsActivity.class);
        map.putExtra("eventId", e.getId());
        startActivity(map);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        System.out.println("latLng");
        if(isCreatingFence){
                Toast.makeText(this, "Tap a spot or tap the button again to complete the fence", Toast.LENGTH_SHORT).show();
                buildEvent.addMarker(latLng, true);

        }
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
        buildEvent.removeMarkers();
        map.addPolygon(buildEvent.getPolyOptions());
        buildEvent.getCenterMarker();
    }

    public void clearMap(){
        isCreatingFence=false;
        buildEvent = new Event(ParseUser.getCurrentUser().getObjectId(), map);
    }

    protected void startLocationUpdates() {
        LocationRequest r = LocationRequest.create();
        r.setInterval(10000);
        r.setFastestInterval(1000);
        r.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, r, this);
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
        loadEvents();

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
        if(marker.equals(buildEvent.centerMarker)){
            getEventName();
        }else{
            for(Event e:events){
                Log.d("haxor", "event: " + e.getName());
                if(e.getCenterMarker().equals(marker)){
                    Log.d("haxor", "starting event: " + e.getName());
                    setEvent(e);
                }
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
                if (name == "") {
                    dialog.cancel();
                    clearMap();
                }

                myFirebaseRef.child("fences").child(user.getObjectId()).child("points").setValue(buildEvent);
                myFirebaseRef.child("fences").child(user.getObjectId()).child("name").setValue(name);

                Toast.makeText(MapActivity.this, "Your fence has been created", Toast.LENGTH_LONG).show();
                setEvent(buildEvent);
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
