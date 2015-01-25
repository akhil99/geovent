package com.buildncode.geovent;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.mapbox.mapboxsdk.views.MapView;


public class MapActivity extends ActionBarActivity{

    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initMapView();
    }

    private void initMapView(){
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setUserLocationEnabled(true);
    }

}
