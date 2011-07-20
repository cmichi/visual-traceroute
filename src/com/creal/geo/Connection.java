package com.creal.geo;

import java.util.List;
import java.util.Vector;

import toxi.geom.Spline3D;
import toxi.geom.Vec3D;

import com.creal.trace.Main;

/**
 * A connection is drawn between two geolocations.
 * 
 * @author cmichi
 */
public class Connection {
	private Main p;

	/** start, destination coordinates */
	public Coordinate co1;
	public Coordinate co2;

	/** color of this connection, see Main.SHINY */
	public int currShiny = 0;

	/** Amplitude of this connection. */
	float thisU = 1.2f;

	private Spline3D sp = new Spline3D();
	
	public int creationTime;
	public boolean finishedDrawing = false;
	public boolean drawingEnabled = false;
	public int strokesDrawn = 0;

	/** a connection can have another connection that is closely 
	 * related. this is used to paint 3 connections for 3 traces on this route.
	 */
	public Connection conn2;


	public Connection(Main main, Coordinate co1, Coordinate co2) {
		this.p = main;
		this.co1 = co1;
		this.co2 = co2;

		/* as an amplitude use a random value
		 * TODO use the trace interval as an amplitude!
		 */
		this.thisU = p.random(1.2f, 1.6f);
	}


	public void draw() {
		if (!drawingEnabled)
			return;

		float scale2 = p.scale * 0.92f;
		sp = new Spline3D();

		/*
		 * is this Connection already completely drawn? if yes enable drawing
		 * the next one.
		 */
		if (strokesDrawn >= 1 && conn2 != null) {
			conn2.enableDrawing();
			conn2.draw();
		}

		p.pushMatrix();
		p.translate(p.width / 2, p.height / 2, p.pushBack);

		p.rotateX(p.radians(-p.rotationX));
		p.rotateY(p.radians(270 - p.rotationY));
		p.rotateY(p.radians(90));

		Vec3D v1 = new Vec3D(0.0f, 0.0f, scale2);
		v1 = v1.rotateX(p.radians(-1.0f * co1.calc("lat", p.scale)));
		v1 = v1.rotateY(p.radians(-1.0f * co1.calc("lon", p.scale)));
		sp.add(v1);

		Vec3D v2 = new Vec3D(0.0f, 0.0f, scale2);
		v2 = v2.rotateX(p.radians(-1.0f * co2.calc("lat", p.scale)));
		v2 = v2.rotateY(p.radians(-1.0f * co2.calc("lon", p.scale)));

		sp.add(getVectorBetween(v1, v2));

		sp.add(v2);

		List<Vec3D> path2 = sp.getDecimatedVertices(0.5f);
		float slot = path2.size() / 90.0f;
		float dest = (p.frameCount - creationTime) * slot;

		if (dest > path2.size() - 1) {
			finishedDrawing = true;
			strokesDrawn++;

			/*
			 * I wanted to use this to paint a mark on the earth texture if
			 * (!marked ) { this.p.earth.paintMark(co2, p.SHINY[currShiny]);
			 * marked = true; }
			 */
		}

		if (drawingEnabled) {
			for (int i = path2.size() - 1; i > dest; i--)
				path2.remove(i);
		}

		p.noFill();
		p.stroke(p.SHINY[currShiny]);
		p.strokeWeight(1);
		p.gfx.lineStrip3D(path2);

		p.popMatrix();
	}


	/**
	 * Give me a vector between v1 and v2.
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	private Vec3D getVectorBetween(Vec3D v1, Vec3D v2) {
		float u = 1.2f;
		u = thisU;
		Vec3D vM = v1.interpolateTo(v2, 0.5f);
		Vec3D vMi = new Vec3D(vM.x * u, vM.y * u, vM.z * u);
		Vec3D mitte = new Vec3D(0.0f, 0.0f, 0.0f);

		if (vMi.distanceTo(mitte) < p.scale) {
			while (vMi.distanceTo(mitte) < p.scale) {
				u += 0.1f;
				vMi = new Vec3D(vM.x * u, vM.y * u, vM.z * u);
			}
		}
		return vMi;
	}


	/**
	 * Enable drawing this connection.
	 */
	public void enableDrawing() {
		if (!drawingEnabled) {
			drawingEnabled = true;
			creationTime = p.frameCount;
		}
	}


	/**
	 * Static method to draw all connections contained in p.connections.
	 * 
	 * @param p
	 */
	public static void drawAllConnections(Main p) {
		Connection prev = null;

		@SuppressWarnings("unchecked")
		Vector<Connection> conns = (Vector<Connection>) p.connections.clone();

		/* -1 because we don't want to paint the last one (it has no successor) */
		for (int i = 0; i < conns.size() - 1; i++) {
			Connection c = conns.get(i);
			if (prev != null && prev.finishedDrawing == true) {
				c.enableDrawing();
				c.draw();
			}

			if (i == 0) { // && c.drawingEnabled == false) {
				/* then it is the first connection, which then can be drawn */
				c.enableDrawing();
				c.draw();
			}

			prev = c;
		}
	}

}
