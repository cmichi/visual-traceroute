package com.creal.geo;

import com.*;
import com.creal.trace.Main;

/**
 * Class is currently not in use, 
 * you can use it to put Markers on the Earth.
 * 
 * Use like:
 * 
 * setup():
 * 	Coordinate greenwhich = new Coordinate(51, "W", 1, "E");
 * 	Coordinate san_francisco = new Coordinate(37, "N", 122, "W");
 * 	Coordinate athen = new Coordinate(37, "N", 23, "E");
 * 
 * draw():
 * 	Marker.drawMarker(this, greenwhich);
 * 
 * @author michi
 */

public class Marker {
	
	public static void drawMarker(Main p, Coordinate sf) {
		float scale2 = p.scale * 1.1f;
		p.pushMatrix();

		p.translate(p.width / 2, p.height / 2, p.pushBack);
		p.rotateX(p.radians(-p.rotationX));
		p.rotateY(p.radians(270 - p.rotationY));

		p.pushMatrix();
		p.rotateY(p.radians(93));
		p.pushMatrix();
		p.rotateY(p.radians(sf.calc("lon", p.scale)));

		p.pushMatrix();
		p.rotateX(p.radians(sf.calc("lat", p.scale)));
		p.stroke(p.color(255, 0, 0));
		p.line(0, 0, 0, 0, 0, scale2);
		p.popMatrix();
		p.popMatrix();
		p.popMatrix();
		p.popMatrix();
	}


}
