package com.creal.geo;

import java.util.ArrayList;

import javax.media.opengl.GL;

import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLModel;
import codeanticode.glgraphics.GLSLShader;
import codeanticode.glgraphics.GLTexture;

import com.*;
import com.creal.trace.Main;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;

/**
 * Okay this code is based on 
 * 
 * 		{@link http://code.google.com/p/processing/source/browse/trunk
 * 		  /processing/java /libraries/opengl/examples/Earth/Earth.pde}
 * 		By Mike 'Flux' Chang (cleaned up by Aaron Koblin). Based on code by Toxi.
 * 		OPENGL2 port by Andres Colubri.
 * 
 * and on the GLGraphics "TexturedSphere" example.
 * 
 * However I did several tweaks and changed stuff around, but this
 * is from what it grew :) .
 */

public class Earth {
	public PImage texmap;
	public PImage normal;

	private Main p;

	private final float SINCOS_PRECISION = 0.5f;
	private final int SINCOS_LENGTH = (int) (360.0f / SINCOS_PRECISION);
	private final float globeRadius = 450.0f;

	private float sinLUT[];
	private float cosLUT[];

	private float[] sphereX;
	private float[] sphereY;
	private float[] sphereZ;
	
	private ArrayList vertices;
	private ArrayList texCoords;
	private ArrayList normals;
	
	private GLSLShader shader;

	private int globeDetail = 50;

	private GLModel earth;
	private GLTexture texEarth;
	private GLTexture texMask;
	private GLTexture texNormal;
	private GLTexture texHeight;
	private GLTexture texSpecular;

	/* camera distance from origin */
	float distance = 30000;
	float sensitivity = 1.0f;
	
	PVector mLightDir;


	public Earth(Main main, String string) {
		this.p = main;

	    /* calculates the vertices, texture coordinates and normals for the earth earth */
	    calculateEarthCoords();

	    earth = new GLModel(this.p, vertices.size(), PApplet.TRIANGLE_STRIP, GLModel.STATIC);
	    
	    /* set coordinates */
	    earth.updateVertices(vertices);
	    
	    texEarth = new GLTexture(this.p, "earthDiffuse.png");
	    texMask = new GLTexture(this.p, "earthMask.png");
	    texNormal = new GLTexture(this.p, "earthNormal.png");
	    
	    texHeight = new GLTexture(this.p, "heightmap.png");
	    texSpecular = new GLTexture(this.p, "earthSpec.png");
	    earth.initTextures(4);
	    earth.setTexture(0, texEarth);
	    earth.setTexture(1, texNormal);
	    earth.setTexture(2, texHeight);
	    earth.setTexture(3, texSpecular);
	    
	    earth.updateTexCoords(0, texCoords);
	    earth.updateTexCoords(1, texCoords);
	    earth.updateTexCoords(2, texCoords);
	    earth.updateTexCoords(3, texCoords);
	    
	    /* set the normals */
	    earth.initNormals();
	    earth.updateNormals(normals);
	    
	    // Sets the colors of all the vertices to white.
	    earth.initColors();
	    earth.setColors(255);
	    
	    shader = new GLSLShader(this.p,  "vert.glsl", "frag.glsl");
	    
		mLightDir = new PVector( 0.025f, 0.25f, 1.0f );
		mLightDir.normalize();
	}


	/**
	 * Compute texture coordinates for the generated earth mesh 
	 */
	void calcTextureCoordinates(TriangleMesh coneMesh) {
		for (Face f : coneMesh.getFaces()) {
			f.uvA = calcUV(f.a);
			f.uvB = calcUV(f.b);
			f.uvC = calcUV(f.c);
		}
	}


	/* compute a 2D texture coordinate from a 3D point on a sphere
	 * this function will be applied to all mesh vertices */
	Vec2D calcUV(Vec3D t) {
		Vec3D s = t.copy().toSpherical();
		Vec2D uv = new Vec2D(s.y / PConstants.TWO_PI,
				1 - (s.z / PConstants.PI + 0.5f));
		/* make sure longitude is always within 0.0 ... 1.0 interval */
		if (uv.x < 0)
			uv.x += 1;
		else if (uv.x > 1)
			uv.x -= 1;
		return uv;
	}
	

	void calculateEarthCoords()
	{
	    float[] cx, cz, sphereX, sphereY, sphereZ;
	    float sinLUT[];
	    float cosLUT[];
	    float delta, angle_step, angle;
	    int vertCount, currVert;
	    float r;
	    float u, v;
	    int v1, v11, v2, voff;
	    float iu, iv;
	      
	    sinLUT = new float[SINCOS_LENGTH];
	    cosLUT = new float[SINCOS_LENGTH];

	    for (int i = 0; i < SINCOS_LENGTH; i++) 
	    {
	        sinLUT[i] = (float) Math.sin(i * PApplet.DEG_TO_RAD * SINCOS_PRECISION);
	        cosLUT[i] = (float) Math.cos(i * PApplet.DEG_TO_RAD * SINCOS_PRECISION);
	    }  
	  
	    delta = (SINCOS_LENGTH / globeDetail);
	    cx = new float[globeDetail];
	    cz = new float[globeDetail];

	    /* Calc unit circle in XZ plane */
	    for (int i = 0; i < globeDetail; i++) 
	    {
	        cx[i] = -cosLUT[(int) (i * delta) % SINCOS_LENGTH];
	        cz[i] = sinLUT[(int) (i * delta) % SINCOS_LENGTH];
	    }

	    /* computing vertexlist vertexlist starts at south pole */
	    vertCount = globeDetail * (globeDetail - 1) + 2;
	    currVert = 0;
	  
	    /* re-init arrays to store vertices */
	    sphereX = new float[vertCount];
	    sphereY = new float[vertCount];
	    sphereZ = new float[vertCount];
	    angle_step = (SINCOS_LENGTH * 0.5f) / globeDetail;
	    angle = angle_step;
	  
	    /* step along Y axis */
	    for (int i = 1; i < globeDetail; i++) 
	    {
	        float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
	        float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
	        for (int j = 0; j < globeDetail; j++) 
	        {
	            sphereX[currVert] = cx[j] * curradius;
	            sphereY[currVert] = currY;
	            sphereZ[currVert++] = cz[j] * curradius;
	        }
	        angle += angle_step;
	    }

	    vertices = new ArrayList();
	    texCoords = new ArrayList();
	    normals = new ArrayList();

	    r = globeRadius;
	    r = (r + 240 ) * 0.33f;

	    iu = 1.0f / globeDetail;
	    iv = 1.0f / globeDetail;
	    
	    // Add the southern cap    
	    u = 0;
	    v = iv;
	    for (int i = 0; i < globeDetail; i++) 
	    {
	        addVertex(0.0f, -r, 0.0f, u, 0);
	        addVertex(sphereX[i] * r, sphereY[i] * r, sphereZ[i] * r, u, v);        
	        u += iu;
	    }
	    addVertex(0.0f, -r, 0.0f, u, 0);
	    addVertex(sphereX[0] * r, sphereY[0] * r, sphereZ[0] * r, u, v);
	  
	    /* middle rings */
	    voff = 0;
	    for (int i = 2; i < globeDetail; i++) 
	    {
	        v1 = v11 = voff;
	        voff += globeDetail;
	        v2 = voff;
	        u = 0;    
	        for (int j = 0; j < globeDetail; j++) 
	        {
	            addVertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1++] * r, u, v);
	            addVertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2++] * r, u, v + iv);
	            u += iu;
	        }
	  
	        /* close each ring */
	        v1 = v11;
	        v2 = voff;
	        addVertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1] * r, u, v);
	        addVertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v + iv);
	        
	        v += iv;
	    }
	    u=0;
	  
	    /* add northern cap */
	    for (int i = 0; i < globeDetail; i++) 
	    {
	        v2 = voff + i;
	     
	        addVertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v);
	        addVertex(0, r, 0, u, v + iv);
	        u+=iu;
	    }
	    addVertex(sphereX[voff] * r, sphereY[voff] * r, sphereZ[voff] * r, u, v);
	}

	
	void addVertex(float x, float y, float z, float u, float v)
	{
		/* bugfix for gap in the texture mapping of earth,
		 * bug results in a thin gap-line in the sphere.
		 */
		u = p.map(u,0,1,0.01f,0.99f);
		v = p.map(v,0,1,0.01f,0.99f);

	    PVector vert = new PVector(x, y, z);
	    PVector texCoord = new PVector(u, v);
	    PVector vertNorm = PVector.div(vert, vert.mag()); 
	    vertices.add(vert);
	    texCoords.add(texCoord);
	    normals.add(vertNorm);
	}


	public void renderGlobe() {
		p.pushMatrix();
		p.translate(p.width / 2.0f, p.height / 2.0f, p.pushBack);
		
		p.rotateX(PApplet.radians(-p.rotationX));
		p.rotateY(PApplet.radians(270 - p.rotationY));

		p.renderer.beginGL();   
		shader.start();
			shader.setIntUniform("texNormal", 1);
			shader.setIntUniform("texHeight", 2);
			shader.setVecUniform("lightDir", mLightDir.x, mLightDir.y, mLightDir.z);
			  
			p.renderer.model(earth);
		shader.stop(); 
	    p.renderer.endGL();
	    
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
		globeDetail = res;
	}


	// Generic routine to draw textured sphere
	void texturedSphere(float r, PImage t) {
		int v1, v11, v2;
		r = (r + 240.0f) * 0.33f;
		p.beginShape(PConstants.TRIANGLE_STRIP);
		p.texture(t);
		float iu = (float) ((t.width - 1) / (globeDetail));
		float iv = (float) ((t.height - 1) / (globeDetail));

		float u = 0, v = iv;
		for (int i = 0; i < globeDetail; i++) {
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
		for (int i = 2; i < globeDetail; i++) {
			v1 = v11 = voff;
			voff += globeDetail;
			v2 = voff;
			u = 0;
			p.beginShape(PConstants.TRIANGLE_STRIP);
			p.texture(t);
			p.texture(normal);
			for (int j = 0; j < globeDetail; j++) {
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
		p.texture(normal);
		for (int i = 0; i < globeDetail; i++) {
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
