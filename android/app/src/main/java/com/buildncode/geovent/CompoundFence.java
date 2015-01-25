package com.buildncode.geovent;

import java.util.*;

public class CompoundFence implements GeoFence{
	ArrayList<GeoFence> fences;
	public CompoundFence(){
		fences = new ArrayList<GeoFence>();
	}
	public void addFence(GeoFence f){
		fences.add(f);
	}
	public boolean contains(Point p){
		for(GeoFence fence: fences){
			if(fence.contains(p)) return true;
		}
		return false;
	}
	public String toString(){
		return fences.toString();
	}
}