package com.buildncode.geovent;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.mapbox.mapboxsdk.views.MapView;


public class MapActivity extends ActionBarActivity{

    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView = new MapView(this);
        mapView.setUserLocationEnabled(true);
        setContentView(mapView);
    }
}
