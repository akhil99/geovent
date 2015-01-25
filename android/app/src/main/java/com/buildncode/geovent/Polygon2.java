package com.buildncode.geovent;
import java.util.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.LatLng;
import android.graphics.Color;


public class Polygon2{
    int N;
    final int MAXC = 10000010;
    Point[] points;
    ArrayList<Point> pointBuf;
    public Polygon2 (ArrayList<Point> pts){
        N = pts.size();
        pointBuf = pts;
    }
    public Polygon2(){
        this(new ArrayList<Point>());
    }
    public void seedPoints(){
        N= pointBuf.size();
        points = new Point[N + 2];
        for(int i = 0; i <  N; i++){
            points[i + 1] = pointBuf.get(i);
        }
        points[0] = points[N]; points[N + 1] = points[1];
    }
    public void addPoint(Point p){
        pointBuf.add(p);
    }
    public boolean isIn(Point p){
        seedPoints();
        int cnt = 0;
        Line tl = new Line(p, new Point(p.x + MAXC, p.y + 1));
        for(int i = 0; i < N; i++){
            Line cl = new Line(points[i], points[i + 1]);
            if(cl.contains(p)) return true;
            if(tl.intersects(cl)) cnt++;
        }
        return (cnt % 2 == 1);
    }
    public String toString(){
        seedPoints();
        return (N + "-gon: \n") + Arrays.toString(points);
    }
    public Polygon2 convexHull(){
        seedPoints();
        ArrayList<Point> ptsnew = new ArrayList<Point>();

        Point fpoint = new Point(MAXC, MAXC);
        for(int i = 1; i <= N; i++){
            if(points[i].x < fpoint.x) fpoint = points[i];
        }

        Point curPoint = fpoint;
        do{
            Point endPoint = points[1];
            for(int i = 1; i <= N; i++){
                if(endPoint == curPoint || Point.ccw(curPoint, endPoint, points[i]) == 1){
                    endPoint = points[i];
                }
            }
            ptsnew.add(endPoint);
            curPoint = endPoint;
        } while(curPoint != fpoint);

        return new Polygon2(ptsnew);
    }
    public static ArrayList<LatLng> getPolyPoints(Polygon2 p){
        ArrayList<LatLng> list = new ArrayList<LatLng> ();
        for(Point pt: p.pointBuf){
            list.add(new LatLng(pt.y, pt.x));
        }
        return list;
    }
}