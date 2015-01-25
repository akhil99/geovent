
package com.buildncode.geovent;

import java.util.*;

public class PolyFence extends Polygon2 implements GeoFence{
    public PolyFence(ArrayList<Point> pts){
        super(pts);
    }
    public PolyFence(){
        super();
    }
    public boolean contains(Point p){
        return isIn(p);
    }
}
