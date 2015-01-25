package com.buildncode.geovent;

import java.util.*;

//simple line class for Geometric point match
class Line{
    Point p1;
    Point p2;
    public Line(double x1, double y1, double x2, double y2){
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
    }
    public Line(Point p1t, Point p2t){
        p1 = p1t;
        p2 = p2t;
    }
    public boolean contains(Point p){
        double dx = p2.x - p.x;
        double dy = p2.y - p.y;
        double ldx = p.x - p1.x;
        double ldy = p.y - p1.y;
        return (in(p.x, p1.x, p2.x) && in(p.y, p1.y, p2.y) && dx*ldy == dy*ldx);
    }
    public boolean intersects(Line l){
        return linesIntersect(p1.x, p1.y, p2.x, p2.y, l.p1.x, l.p1.y, l.p2.x, l.p2.y);
    }

    public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3,
                                         double y3, double x4, double y4) {
        /*
         * A = (x2-x1, y2-y1) B = (x3-x1, y3-y1) C = (x4-x1, y4-y1) D = (x4-x3,
         * y4-y3) = C-B E = (x1-x3, y1-y3) = -B F = (x2-x3, y2-y3) = A-B Result
         * is ((AxB) (AxC) <=0) and ((DxE) (DxF) <= 0) DxE = (C-B)x(-B) =
         * BxB-CxB = BxC DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB = AxB+BxC-AxC
         */
        x2 -= x1; // A
        y2 -= y1;
        x3 -= x1; // B
        y3 -= y1;
        x4 -= x1; // C
        y4 -= y1;
        double AvB = x2 * y3 - x3 * y2;
        double AvC = x2 * y4 - x4 * y2;
        // Online
        if (AvB == 0.0 && AvC == 0.0) {
            if (x2 != 0.0) {
                return (x4 * x3 <= 0.0)
                        || ((x3 * x2 >= 0.0) && (x2 > 0.0 ? x3 <= x2 || x4 <= x2 : x3 >= x2
                        || x4 >= x2));
            }
            if (y2 != 0.0) {
                return (y4 * y3 <= 0.0)
                        || ((y3 * y2 >= 0.0) && (y2 > 0.0 ? y3 <= y2 || y4 <= y2 : y3 >= y2
                        || y4 >= y2));
            }
            return false;
        }
        double BvC = x3 * y4 - x4 * y3;
        return (AvB * AvC <= 0.0) && (BvC * (AvB + BvC - AvC) <= 0.0);
    }
    public static boolean in(double t, double a, double b){
        return (t >= Math.min(a, b) && t <= Math.max(a, b));
    }
    public String toString(){
        return p1 + " -----> " + p2;
    }
}