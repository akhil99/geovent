package com.buildncode.geovent;


import com.google.android.gms.maps.model.LatLng;

public class Point{
    double x;
    double y;

    public Point(LatLng latLng){
        this(latLng.latitude, latLng.longitude);
    }

    public Point(double px, double py){
        x = px;
        y = py;
    }
    public String toString(){
        return "(" + x + ", " + y + ")";
    }
    public boolean equals(Point p2){
        return x == p2.x && y == p2.y;
    }
    public double getDist(Point p2){
        return Math.sqrt((x - p2.x)* (x - p2.x) + (y - p2.y)*(y - p2.y));
    }
    public static int ccw(Point a, Point b, Point c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if      (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else                return  0;
    }

}