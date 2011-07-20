package com.creal.geo;

import com.*;
import com.creal.trace.Main;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;

/**
 * By Mike 'Flux' Chang (cleaned up by Aaron Koblin). Based on code by Toxi.
 * OPENGL2 port by Andres Colubri.
 * 
 * I did some minor tweaks for this project. Original version can be found here:
 * 
 * {@link http://code.google.com/p/processing/source/browse/trunk
 * 		  /processing/java /libraries/opengl/examples/Earth/Earth.pde}
 */

public class Earth {
	public PImage texmap;
	private PImage texmapOriginal;

	private Main p;

	private final float SINCOS_PRECISION = 0.5f;
	private final int SINCOS_LENGTH = (int) (360.0f / SINCOS_PRECISION);
	private final float globeRadius = 450;

	private int sDetail = 35; // Sphere detail setting

	private float sinLUT[];
	private float cosLUT[];

	float[] cx, cz, sphereX, sphereY, sphereZ;


	public Earth(Main main, String string) {
		this.p = main;

		/** why we clone it? See method {@see paintMark}. */
		texmapOriginal = p.loadImage("world32k.jpg");
		try {
			texmap = (PImage) texmapOriginal.clone();
		} catch (Exception e) {
		}

		initializeSphere(sDetail);
	}


	// this function computes texture coordinates
	// for the generated earth mesh
	void calcTextureCoordinates(TriangleMesh coneMesh) {
		for (Face f : coneMesh.getFaces()) {
			f.uvA = calcUV(f.a);
			f.uvB = calcUV(f.b);
			f.uvC = calcUV(f.c);
		}
	}


	// compute a 2D texture coordinate from a 3D point on a sphere
	// this function will be applied to all mesh vertices
	Vec2D calcUV(Vec3D t) {
		Vec3D s = t.copy().toSpherical();
		Vec2D uv = new Vec2D(s.y / PConstants.TWO_PI,
				1 - (s.z / PConstants.PI + 0.5f));
		// make sure longitude is always within 0.0 ... 1.0 interval
		if (uv.x < 0)
			uv.x += 1;
		else if (uv.x > 1)
			uv.x -= 1;
		return uv;
	}


	public void renderGlobe() {
		p.pushMatrix();
		p.translate(p.width / 2.0f, p.height / 2.0f, p.pushBack);
		p.pushMatrix();
		p.noFill();
		p.stroke(255, 200);
		p.strokeWeight(2);
		p.smooth();
		p.popMatrix();
		/* p.lights(); */

		p.pushMatrix();
		p.rotateX(PApplet.radians(-p.rotationX));
		p.rotateY(PApplet.radians(270 - p.rotationY));
		p.fill(200);
		p.noStroke();
		p.textureMode(PConstants.IMAGE);

		texturedSphere(globeRadius, texmap);

		p.popMatrix();
		p.popMatrix();
		p.rotationX += p.velocityX;
		p.rotationY += p.velocityY;
		p.velocityX *= 0.95f;
		p.velocityY *= 0.95f;
	}


	public void initializeSphere(int res) {
		sinLUT = new float[SINCOS_LENGTH];
		cosLUT = new float[SINCOS_LENGTH];

		for (int i = 0; i < SINCOS_LENGTH; i++) {
			sinLUT[i] = (float) Math.sin(i * PConstants.DEG_TO_RAD
					* SINCOS_PRECISION);
			cosLUT[i] = (float) Math.cos(i * PConstants.DEG_TO_RAD
					* SINCOS_PRECISION);
		}

		float delta = (float) SINCOS_LENGTH / res;
		float[] cx = new float[res];
		float[] cz = new float[res];

		// Calc unit circle in XZ plane
		for (int i = 0; i < res; i++) {
			cx[i] = -cosLUT[(int) (i * delta) % SINCOS_LENGTH];
			cz[i] = sinLUT[(int) (i * delta) % SINCOS_LENGTH];
		}

		// Computing vertexlist vertexlist starts at south pole
		int vertCount = res * (res - 1) + 2;
		int currVert = 0;

		// Re-init arrays to store vertices
		sphereX = new float[vertCount];
		sphereY = new float[vertCount];
		sphereZ = new float[vertCount];
		float angle_step = (SINCOS_LENGTH * 0.5f) / res;
		float angle = angle_step;

		// Step along Y axis
		for (int i = 1; i < res; i++) {
			float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
			float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
			for (int j = 0; j < res; j++) {
				sphereX[currVert] = cx[j] * curradius;
				sphereY[currVert] = currY;
				sphereZ[currVert++] = cz[j] * curradius;
			}
			angle += angle_step;
		}
		sDetail = res;
	}


	// Generic routine to draw textured sphere
	void texturedSphere(float r, PImage t) {
		int v1, v11, v2;
		r = (r + 240.0f) * 0.33f;
		p.beginShape(PConstants.TRIANGLE_STRIP);
		p.texture(t);
		float iu = (float) (t.width - 1) / (sDetail);
		float iv = (float) (t.height - 1) / (sDetail);

		float u = 0, v = iv;
		for (int i = 0; i < sDetail; i++) {
			p.normal(0, -1, 0);
			p.vertex(0, -r, 0, u, 0);
			p.normal(sphereX[i], sphereY[i], sphereZ[i]);
			p.vertex(sphereX[i] * r, sphereY[i] * r, sphereZ[i] * r, u, v);
			u += iu;
		}
		p.vertex(0, -r, 0, u, 0);
		p.normal(sphereX[0], sphereY[0], sphereZ[0]);
		p.vertex(sphereX[0] * r, sphereY[0] * r, sphereZ[0] * r, u, v);
		p.endShape();

		// Middle rings
		int voff = 0;
		for (int i = 2; i < sDetail; i++) {
			v1 = v11 = voff;
			voff += sDetail;
			v2 = voff;
			u = 0;
			p.beginShape(PConstants.TRIANGLE_STRIP);
			p.texture(t);
			for (int j = 0; j < sDetail; j++) {
				p.normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
				p.vertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1++] * r,
						u, v);
				p.normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
				p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2++] * r,
						u, v + iv);
				u += iu;
			}

			// Close each ring
			v1 = v11;
			v2 = voff;
			p.normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
			p.vertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1] * r, u, v);
			p.normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
			p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v
					+ iv);
			p.endShape();
			v += iv;
		}
		u = 0;

		// Add the northern cap
		p.beginShape(PConstants.TRIANGLE_STRIP);
		p.texture(t);
		for (int i = 0; i < sDetail; i++) {
			v2 = voff + i;
			p.normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
			p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v);
			p.normal(0, 1, 0);
			p.vertex(0, r, 0, u, v + iv);
			u += iu;
		}
		p.normal(sphereX[voff], sphereY[voff], sphereZ[voff]);
		p.vertex(sphereX[voff] * r, sphereY[voff] * r, sphereZ[voff] * r, u, v);

		p.endShape();
	}


	/**
	 * Can be used to draw a mark on the texture. A circle for example.
	 * 
	 * @param co
	 * @param color
	 */
	public void paintMark(Coordinate co, int color) {
		for (double ra = 5.0d; ra >= 0.0d; ra -= 0.5d) {
			for (double e = 0; e < PConstants.TWO_PI; e += 0.1f) {
				this.texmap.set((int) (co.x + ra * Math.sin(e)),
						(int) (co.y + ra * Math.cos(e)), color);
			}
		}
	}
}
