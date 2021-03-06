package com.buildncode.geovent;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class EventsActivity extends ActionBarActivity implements ActionBar.TabListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each  of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    SupportMapFragment mapFragment;
    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    ParseUser user;

    LocationRequest locationRequest;

    ArrayList<PolygonOptions> queue;

    boolean mapMoved = false;

    Firebase myFirebaseRef;

    String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        eventID = getIntent().getStringExtra("eventId");
        Log.d("HAXORS", eventID);

        mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);

        user = ParseUser.getCurrentUser();
        queue = new ArrayList<PolygonOptions>();

        buildGoogleApiClient();

        initFirebase();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    private void initFirebase(){
        myFirebaseRef = new Firebase("https://geovent.firebaseio.com/");
        myFirebaseRef.child("fences").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(eventID)) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    getSupportActionBar().setTitle("Event: " + name);

                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    long cnt = dataSnapshot.child("points").getChildrenCount();
                    PolygonOptions polyOptions = new PolygonOptions();

                    for (int i = 0; i < cnt; i++) {
                        Double lat = (Double) dataSnapshot.child("points").child("" + i).child("latitude").getValue();
                        Double lon = (Double) dataSnapshot.child("points").child("" + i).child("longitude").getValue();
                        LatLng ll = new LatLng(lat.doubleValue(), lon.doubleValue());
                        points.add(ll);
                        polyOptions.add(ll);
                    }
                    polyOptions.fillColor(Color.argb(150, 89, 89, 94));
                    polyOptions.visible(true);
                    polyOptions.strokeColor(Color.argb(252, 89, 89, 94));
                    polyOptions.strokeWidth(10);

                    if(map != null)map.addPolygon(polyOptions);
                    else queue.add(polyOptions);
                    Log.d("haxors", "name: " + dataSnapshot.child("name").getValue(String.class));

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

        myFirebaseRef.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                Log.d("HAXORS", "child added, key: " + snapshot.getKey());
                if(snapshot.getKey().equals(ParseUser.getCurrentUser().getObjectId())) {
                    Log.d("HAXORS", "same key: " + snapshot.getKey());
                }else if(snapshot.child("event").getValue().equals(eventID)){
                    Log.d("HAXORS", "correct event");
                    addUser(snapshot.getKey());

                }else removeUser(snapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                Log.d("HAXORS", "child changed, key: " + snapshot.getKey());
                if(snapshot.getKey().equals(ParseUser.getCurrentUser().getObjectId())){
                    Log.d("HAXORS", "same key: " + snapshot.getKey());
                }else if(snapshot.child("event").getValue().equals(eventID)){
                    Log.d("HAXORS", "correct event");
                    addUser(snapshot.getKey());
                }else removeUser(snapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                removeUser(snapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    Map<String, ValueEventListener> userListeners = new HashMap<String, ValueEventListener>();
    Map<String, Marker> userMarkers = new HashMap<String, Marker>();

    public void addUser(String userID){
        Log.d("HAXORS", "add user");
        if(userListeners.get(userID) == null)userListeners.put(userID, getValueEventListener(userID));
        Firebase ref = myFirebaseRef.child("users").child(userID);
        ref.addValueEventListener(userListeners.get(userID));
    }

    public void removeUser(String userID){
        Log.d("HAXORS", "remove user");
        Firebase ref = myFirebaseRef.child("users").child(userID);
        if(userListeners.get(userID) != null){
            ref.removeEventListener(userListeners.get(userID));
            userListeners.remove(userID);
        }
        if(userMarkers.get(userID) != null){
            userMarkers.get(userID).remove();
            userMarkers.put(userID, null);
        }
    }

    private ValueEventListener getValueEventListener(final String userID){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.child("event").getValue()  != null) {
                    double lat = snapshot.child("latitude").getValue(Double.class);
                    double lon = snapshot.child("longitude").getValue(Double.class);
                    LatLng ll = new LatLng(lat, lon);
                    if (userMarkers.get(userID) == null) {
                        Marker m = map.addMarker(new MarkerOptions().position(ll).title(userID).snippet(""));
                        userMarkers.put(userID, m);
                    }
                    userMarkers.get(userID).setPosition(new LatLng(lat, lon));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        };
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
        startLocationUpdates();
    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onStop(){
        super.onStop();
        if(locationRequest != null)LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        myFirebaseRef.child("users").child(ParseUser.getCurrentUser().getObjectId()).removeValue();
    }

    public void onDestroy(){
        super.onDestroy();

        if(mGoogleApiClient != null)mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("haxor", "map ready");
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.zoomTo(17));
        for(PolygonOptions o:queue)map.addPolygon(o);
    }

    protected void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return mapFragment;
                case 1:
                    return MembersFragment.newInstance(2);
                case 2:
                    return ChatFragment.newInstance(3);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.events_tab_name);
                case 1:
                    return getString(R.string.members_tab_name);
                case 2:
                    return getString(R.string.chat_tab_name);
            }
            return null;
        }
    }

    public static class MembersFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MembersFragment newInstance(int sectionNumber) {
            MembersFragment fragment = new MembersFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        public MembersFragment() {
        }
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                View rootView = inflater.inflate(R.layout.fragment_members, container, false);

                String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                        "Linux", "OS/2" };
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, values);
                setListAdapter(adapter);

                return rootView;
                //return inflater.inflate(R.layout.fragment_members, container, false);
            }
     /*   @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_members, container, false);

            String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                    "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                    "Linux", "OS/2" };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);

            return rootView;
        }*/
    }

    public static class ChatFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ChatFragment newInstance(int sectionNumber) {
            ChatFragment fragment = new ChatFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
            return rootView;
        }
    }

}
