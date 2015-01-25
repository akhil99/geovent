package com.buildncode.geovent;

import android.app.Application;
import android.content.Intent;

import com.parse.Parse;

/**
 * Created by Akhil on 1/24/2015.
 */
public class App extends Application {

    public void onCreate() {
        Parse.initialize(this, "2oQh21PTJ6V1DsMueOpuqizeg75X9hDByaHfbSOj", "KV0sbEohfPsIpEK0WnFPM4TUNqOFIxiofjUMVYIx");
    }

}
