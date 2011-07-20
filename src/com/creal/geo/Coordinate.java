package com.creal.geo;

import com.creal.trace.Main;

import processing.core.PConstants;

/**
 * 
 * @author michi
 */

public class Coordinate {
	private float earthTextureWidth = 1350.0f;
	private float earthTextureHeight = 675.0f;

	public double y;
	public double x;

	public double lon;
	public double lat;
	char dirLat;
	char dirLon;

	/* position on the sphere */
	public float x_, y_, z_;

	@SuppressWarnings("unused")
	private Main p;
	
	public Coordinate(Main main, String line) {
		this.p = main;

		String[] cs = line.split(" ");
		try {
			lat = Double.parseDouble(cs[0].trim());
			if (lat < 0) {
				lat = Math.abs(lat);
				dirLat = 'S';
			} else
				dirLat = 'N';

			lon = Double.parseDouble(cs[1].trim());
			if (lon < 0) {
				lon = Math.abs(lon);
				dirLon = 'W';
			} else
				dirLon = 'E';
			
			calcXY(lat, dirLat, lon, dirLon);
		} catch (NumberFormatException nfe) {
			System.out.println("NumberFormatException: " + nfe.getMessage());
		}
	}
	
	/**
	 * Create Coordinate from a call like
	 * {@code new Coordinate(51, "W", 1, "E");}.
	 * 
	 * @param latitude
	 * @param dirLa
	 * @param longitude
	 * @param dirLo
	 */
	public void calcXY(double latitude, char dirLa, double longitude, char dirLo) {
		float halfWidth = earthTextureWidth / 2.0f;
		float halfHeight = earthTextureHeight / 2.0f;

		if (dirLo == 'E')
			x = halfWidth + ((halfWidth / 180.0d) * longitude);
		else
			/* W */
			x = halfWidth - ((halfWidth / 180.0d) * longitude);

		if (dirLa == 'N')
			y = halfHeight - ((halfHeight / 93.0d) * latitude);
		else
			/* S */
			y = halfHeight + ((halfHeight / 93.0d) * latitude);
		
		//System.out.println(x + ", " + y);

		/* not elegant. well.. it works. */
		x += 15;
		//x -= 203;
		//y -= 190;
	}

	/**
	 * Is this Coordiante equal to another? Yes if it is on the same Lon & Lat,
	 * within +/- eps.
	 * 
	 * @param co
	 * @return
	 */
	public boolean equals(Coordinate co) {
		float eps = 0.0000001f;
		if ((co.lat == lat && co.lon == lon && co.dirLat == dirLat && co.dirLon == dirLon)
				||
				(co.lat >= lat - eps && co.lat <= lat + eps
						&& co.lon >= lon - eps && co.lon <= lon + eps
						&& co.dirLat == dirLat && co.dirLon == dirLon))
			return true;

		return false;
	}

	public float calc(String what, float r) {
		// pi * 500 = 1570 --  circumference of the circle
		// 1570 / 4 = 392
		// 392 / 90 = 4.355

		// drehung nach oben
		// 4.355 * 37 = 161
		// (161 * 180) / (math.pi * 250)

		float dir = 1.0f;

		if (what.equals("lat") && dirLat == 'S')
			dir = -1.0f;

		if (what.equals("lon") && dirLon == 'W')
			dir = -1.0f;

		if (what.equals("lat"))
			return (float) (((4.355f * lat) * 180.0f) / (PConstants.PI * r) * dir);
		else if (what.equals("lon"))
			return (float) (((4.355f * lon) * 180.0f) / (PConstants.PI * r) * dir);

		return 0.0f;
	}

	public String toString() {
		return lat + " " + dirLat + ", " + lon + " " + dirLon;
	}

}
