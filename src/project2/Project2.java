package project2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;
import graphicslib3D.Vertex3D;
import graphicslib3D.shape.Sphere;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL4.*;

/**
 * Project 2: 3D Modeling and Camera Manipulation
 * <p>
 * Based on Program 4.3 - Multiple Models and Program 6.1 - Sphere from Gordon & Clevenger.
 *
 * @author Eric Peterson
 */
public class Project2 extends JFrame implements GLEventListener, KeyListener
{
	/* ********* *
	 * Constants *
	 * ********* */
	private static final int SPHERE_PRECISION = 24;
	private static final String EARTH_TEXTURE_FILE = "textures/earth.jpg";
	private static final String SUN_TEXTURE_FILE = "textures/sun.jpg";
	private static final String EARTH_MOON_TEXTURE_FILE = "textures/moon.jpg";
	private static final String MARS_TEXTURE_FILE = "textures/mars.jpg";
	
	/* **************** *
	 * Member Variables *
	 * **************** */
	private GLCanvas m_myCanvas;
	private int m_renderingProgram;
	private int[] m_vao;
	private int[] m_vbo;
	private MatrixStack m_mvStack;
	private float m_cameraX, m_cameraY, m_cameraZ;
	private float m_cubeLocX, m_cubeLocY, m_cubeLocZ;
	private float m_pyrLocX, m_pyrLocY, m_pyrLocZ;
	private float m_sunLocX, m_sunLocY, m_sunLocZ;
	private FPSAnimator m_animator;
	private Sphere m_sun;
	private Sphere m_earth;
	private Sphere m_earthMoon;
	private Sphere m_mars;
	private int m_sunTexture;
	private int m_earthTexture;
	private int m_earthMoonTexture;
	private int m_marsTexture;
	private Texture m_joglSunTexture;
	private Texture m_joglEarthTexture;
	private Texture m_joglEarthMoonTexture;
	private Texture m_joglMarsTexture;
	
	public Project2()
	{
		// Initialize default member variable values.
		m_vao = new int[1];
		m_vbo = new int[12];
		m_mvStack = new MatrixStack(20);
		m_sun = new Sphere(SPHERE_PRECISION);
		m_earth = new Sphere(SPHERE_PRECISION);
		m_earthMoon = new Sphere(SPHERE_PRECISION);
		m_mars = new Sphere(SPHERE_PRECISION);
		
		// Set up JFrame properties.
		setTitle("Project 2 - 3D Modeling and Camera Manipulation");
		setSize(980, 980);
		m_myCanvas = new GLCanvas();
		m_myCanvas.addGLEventListener(this);
		m_myCanvas.addKeyListener(this);
		getContentPane().add(m_myCanvas);
		this.setVisible(true);
		m_animator = new FPSAnimator(m_myCanvas, 30);
		m_animator.start();
	}
	
	public void display(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Clear the depth buffer so no trails are left behind.
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glUseProgram(m_renderingProgram);
		
		// Get the memory locations of the uniforms in the shaders.
		int mvLoc = gl.glGetUniformLocation(m_renderingProgram, "mv_matrix");
		int projLoc = gl.glGetUniformLocation(m_renderingProgram, "proj_matrix");
		
		// Construct perspective projection matrix.
		float aspect = (float) m_myCanvas.getWidth() / (float) m_myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		// Set up view matrix.
		m_mvStack.pushMatrix();
		m_mvStack.translate(-m_cameraX, -m_cameraY, -m_cameraZ);
		double amt = (System.currentTimeMillis()) / 1000.0;
		
		// Pass the projection matrix to a uniform in the shader.
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		/* *** *
		 * Sun *
		 * *** */
		
		// Apply transformations to the model-view matrix.
		m_mvStack.pushMatrix();
		m_mvStack.translate(m_sunLocX, m_sunLocY, m_sunLocZ);
		m_mvStack.pushMatrix();
		m_mvStack.rotate(((System.currentTimeMillis()) / 100.0) % 360, 0.0, 1.0, 0.0);
		
		// Pass the model-view matrix to a uniform in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, m_mvStack.peek().getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// Set up texture.
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_sunTexture);
		
		// Enable depth test and face-culling.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		int numVerts = m_sun.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		m_mvStack.popMatrix();
		
		/* ***** *
		 * Earth *
		 * ***** */
		
		// Apply transformations to the model-view matrix.
		m_mvStack.pushMatrix();
		m_mvStack.translate(Math.sin(amt) * 4.0f, 0.0f, Math.cos(amt) * 4.0f);
		m_mvStack.pushMatrix();
		m_mvStack.rotate(((System.currentTimeMillis()) / 50.0) % 360, 0.0, 1.0, 0.0);
		m_mvStack.scale(0.75, 0.75, 0.75);
		
		// Pass the model-view matrix to a uniform in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, m_mvStack.peek().getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[4]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// Set up texture.
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_earthTexture);
		
		// Enable depth test and face-culling.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		numVerts = m_earth.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		m_mvStack.popMatrix();
		
		/* ************ *
		 * Earth's Moon *
		 * ************ */
		
		// Apply transformations to the model-view matrix.
		m_mvStack.pushMatrix();
		m_mvStack.translate(0.0f, Math.sin(amt) * 2.0f, Math.cos(amt) * 2.0f);
		m_mvStack.rotate(((System.currentTimeMillis()) / 10.0) % 360, 0.0, 0.0, 1.0);
		m_mvStack.scale(0.25, 0.25, 0.25);
		
		// Pass the model-view matrix to a uniform in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, m_mvStack.peek().getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[7]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// Set up texture.
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_earthMoonTexture);
		
		// Enable depth test and face-culling.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		numVerts = m_earthMoon.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		m_mvStack.popMatrix();
		
		// Go back to sun reference.
		m_mvStack.popMatrix();
		
		/* **** *
		 * Mars *
		 * **** */
		
		// Apply transformations to the model-view matrix.
		m_mvStack.pushMatrix();
		m_mvStack.translate(Math.sin(amt * 1.5) * 6.0f, Math.sin(amt * 1.5) * 6.0f, Math.cos(amt * 1.5) * 6.0f);
		m_mvStack.pushMatrix();
		m_mvStack.rotate(((System.currentTimeMillis()) / 40.0) % 360, 0.0, 1.0, 0.0);
		m_mvStack.scale(0.60, 0.60, 0.60);
		
		// Pass the model-view matrix to a uniform in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, m_mvStack.peek().getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[9]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[10]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// Set up texture.
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_marsTexture);
		
		// Enable depth test and face-culling.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		numVerts = m_mars.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		m_mvStack.popMatrix();
		
		m_mvStack.popMatrix();
		m_mvStack.popMatrix();
		m_mvStack.popMatrix();
		
		
		/*// Apply transformations to the model matrix.
		Matrix3D mMat = new Matrix3D();
		mMat.translate(m_sunLocX, m_sunLocY, m_sunLocZ);
		mMat.rotate(0, ((double) (System.currentTimeMillis() - m_startTime) / 100) % 360, 0);
		
		// Construct model-view matrix.
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		// Pass the model-view and projection matrices to uniforms in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_sunTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		numVerts = m_sun.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		
		*//* ***** *
	 * Earth *
	 * ***** *//*
		
		// Apply transformations to the model matrix.
		mMat = new Matrix3D();
		mMat.translate(m_earthLocX, m_earthLocY, m_earthLocZ);
		mMat.rotate(0, ((double) (System.currentTimeMillis() - m_startTime) / 50) % 360, 0);
		
		// Construct model-view matrix.
		mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		// Pass the model-view and projection matrices to uniforms in the shader.
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		// Bind the vertex buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Bind the texture buffer to a vertex attribute.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[4]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, m_earthTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		// Draw the object.
		numVerts = m_earth.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);*/
		
		/*// ------------------- draw the cube using buffer #0
		Matrix3D mMat = new Matrix3D();
		mMat.translate(m_cubeLocX, m_cubeLocY, m_cubeLocZ);
		
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		
		// ------------------- draw the pyramid using buffer #1
		mMat = new Matrix3D();
		mMat.translate(m_pyrLocX, m_pyrLocY, m_pyrLocZ);
		
		mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 18);*/
	}
	
	public void init(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		m_renderingProgram = createShaderProgram();
		setupVertices();
		
		// Camera Position
		m_cameraX = 0.0f;
		m_cameraY = 0.0f;
		m_cameraZ = 15.0f;
		
		m_cubeLocX = 0.0f;
		m_cubeLocY = -2.0f;
		m_cubeLocZ = 0.0f;
		m_pyrLocX = 2.0f;
		m_pyrLocY = 2.0f;
		m_pyrLocZ = 0.0f;
		
		// Sun Position
		m_sunLocX = 0.0f;
		m_sunLocY = 0.0f;
		m_sunLocZ = 0.0f;
		
		// Sun Texture
		m_joglSunTexture = loadTexture(SUN_TEXTURE_FILE);
		m_sunTexture = m_joglSunTexture.getTextureObject();
		
		// Earth Texture
		m_joglEarthTexture = loadTexture(EARTH_TEXTURE_FILE);
		m_earthTexture = m_joglEarthTexture.getTextureObject();
		
		// Earth's Moon Texture
		m_joglEarthMoonTexture = loadTexture(EARTH_MOON_TEXTURE_FILE);
		m_earthMoonTexture = m_joglEarthMoonTexture.getTextureObject();
		
		// Mars Texture
		m_joglMarsTexture = loadTexture(MARS_TEXTURE_FILE);
		m_marsTexture = m_joglMarsTexture.getTextureObject();
	}
	
	private void setupVertices()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Set up vertex array.
		gl.glGenVertexArrays(m_vao.length, m_vao, 0);
		gl.glBindVertexArray(m_vao[0]);
		gl.glGenBuffers(m_vbo.length, m_vbo, 0);
		
		/* *** *
		 * Sun *
		 * *** */
		
		// Get sun vertices and indices.
		Vertex3D[] sunVertices = m_sun.getVertices();
		int[] sunIndices = m_sun.getIndices();
		
		// Create vertex, texture, and normal buffers.
		float[] pSunValues = new float[sunIndices.length * 3];
		float[] tSunValues = new float[sunIndices.length * 2];
		float[] nSunValues = new float[sunIndices.length * 3];
		
		// Populate the buffers with the proper values.
		for(int i = 0; i < sunIndices.length; i++)
		{
			pSunValues[i * 3] = (float) (sunVertices[sunIndices[i]]).getX();
			pSunValues[i * 3 + 1] = (float) (sunVertices[sunIndices[i]]).getY();
			pSunValues[i * 3 + 2] = (float) (sunVertices[sunIndices[i]]).getZ();
			tSunValues[i * 2] = (float) (sunVertices[sunIndices[i]]).getS();
			tSunValues[i * 2 + 1] = (float) (sunVertices[sunIndices[i]]).getT();
			nSunValues[i * 3] = (float) (sunVertices[sunIndices[i]]).getNormalX();
			nSunValues[i * 3 + 1] = (float) (sunVertices[sunIndices[i]]).getNormalY();
			nSunValues[i * 3 + 2] = (float) (sunVertices[sunIndices[i]]).getNormalZ();
		}
		
		// Bind vertex buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		FloatBuffer sunVertBuf = Buffers.newDirectFloatBuffer(pSunValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunVertBuf.limit() * 4, sunVertBuf, GL_STATIC_DRAW);
		
		// Bind texture buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		FloatBuffer sunTexBuf = Buffers.newDirectFloatBuffer(tSunValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunTexBuf.limit() * 4, sunTexBuf, GL_STATIC_DRAW);
		
		// Bind normal buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[2]);
		FloatBuffer sunNormalBuf = Buffers.newDirectFloatBuffer(nSunValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunNormalBuf.limit() * 4, sunNormalBuf, GL_STATIC_DRAW);
		
		/* ***** *
		 * Earth *
		 * ***** */
		
		// Get earth vertices and indices.
		Vertex3D[] earthVertices = m_earth.getVertices();
		int[] earthIndices = m_earth.getIndices();
		
		// Create vertex, texture, and normal buffers.
		float[] pEarthValues = new float[earthIndices.length * 3];
		float[] tEarthValues = new float[earthIndices.length * 2];
		float[] nEarthValues = new float[earthIndices.length * 3];
		
		// Populate the buffers with the proper values.
		for(int i = 0; i < earthIndices.length; i++)
		{
			pEarthValues[i * 3] = (float) (earthVertices[earthIndices[i]]).getX();
			pEarthValues[i * 3 + 1] = (float) (earthVertices[earthIndices[i]]).getY();
			pEarthValues[i * 3 + 2] = (float) (earthVertices[earthIndices[i]]).getZ();
			tEarthValues[i * 2] = (float) (earthVertices[earthIndices[i]]).getS();
			tEarthValues[i * 2 + 1] = (float) (earthVertices[earthIndices[i]]).getT();
			nEarthValues[i * 3] = (float) (earthVertices[earthIndices[i]]).getNormalX();
			nEarthValues[i * 3 + 1] = (float) (earthVertices[earthIndices[i]]).getNormalY();
			nEarthValues[i * 3 + 2] = (float) (earthVertices[earthIndices[i]]).getNormalZ();
		}
		
		// Bind vertex buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[3]);
		FloatBuffer earthVertBuf = Buffers.newDirectFloatBuffer(pEarthValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthVertBuf.limit() * 4, earthVertBuf, GL_STATIC_DRAW);
		
		// Bind texture buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[4]);
		FloatBuffer earthTexBuf = Buffers.newDirectFloatBuffer(tEarthValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthTexBuf.limit() * 4, earthTexBuf, GL_STATIC_DRAW);
		
		// Bind normal buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[5]);
		FloatBuffer earthNormalBuf = Buffers.newDirectFloatBuffer(nEarthValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthNormalBuf.limit() * 4, earthNormalBuf, GL_STATIC_DRAW);
		
		/* ************ *
		 * Earth's Moon *
		 * ************ */
		
		// Get earth vertices and indices.
		Vertex3D[] earthMoonVertices = m_earthMoon.getVertices();
		int[] earthMoonIndices = m_earthMoon.getIndices();
		
		// Create vertex, texture, and normal buffers.
		float[] pEarthMoonValues = new float[earthMoonIndices.length * 3];
		float[] tEarthMoonValues = new float[earthMoonIndices.length * 2];
		float[] nEarthMoonValues = new float[earthMoonIndices.length * 3];
		
		// Populate the buffers with the proper values.
		for(int i = 0; i < earthMoonIndices.length; i++)
		{
			pEarthMoonValues[i * 3] = (float) (earthMoonVertices[earthMoonIndices[i]]).getX();
			pEarthMoonValues[i * 3 + 1] = (float) (earthMoonVertices[earthMoonIndices[i]]).getY();
			pEarthMoonValues[i * 3 + 2] = (float) (earthMoonVertices[earthMoonIndices[i]]).getZ();
			tEarthMoonValues[i * 2] = (float) (earthMoonVertices[earthMoonIndices[i]]).getS();
			tEarthMoonValues[i * 2 + 1] = (float) (earthMoonVertices[earthMoonIndices[i]]).getT();
			nEarthMoonValues[i * 3] = (float) (earthMoonVertices[earthMoonIndices[i]]).getNormalX();
			nEarthMoonValues[i * 3 + 1] = (float) (earthMoonVertices[earthMoonIndices[i]]).getNormalY();
			nEarthMoonValues[i * 3 + 2] = (float) (earthMoonVertices[earthMoonIndices[i]]).getNormalZ();
		}
		
		// Bind vertex buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[6]);
		FloatBuffer earthMoonVertBuf = Buffers.newDirectFloatBuffer(pEarthMoonValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthMoonVertBuf.limit() * 4, earthMoonVertBuf, GL_STATIC_DRAW);
		
		// Bind texture buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[7]);
		FloatBuffer earthMoonTexBuf = Buffers.newDirectFloatBuffer(tEarthMoonValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthMoonTexBuf.limit() * 4, earthMoonTexBuf, GL_STATIC_DRAW);
		
		// Bind normal buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[8]);
		FloatBuffer earthMoonNormalBuf = Buffers.newDirectFloatBuffer(nEarthMoonValues);
		gl.glBufferData(GL_ARRAY_BUFFER, earthMoonNormalBuf.limit() * 4, earthMoonNormalBuf, GL_STATIC_DRAW);
		
		/* **** *
		 * Mars *
		 * **** */
		
		// Get earth vertices and indices.
		Vertex3D[] marsVertices = m_mars.getVertices();
		int[] marsIndices = m_mars.getIndices();
		
		// Create vertex, texture, and normal buffers.
		float[] pMarsValues = new float[marsIndices.length * 3];
		float[] tMarsValues = new float[marsIndices.length * 2];
		float[] nMarsValues = new float[marsIndices.length * 3];
		
		// Populate the buffers with the proper values.
		for(int i = 0; i < marsIndices.length; i++)
		{
			pMarsValues[i * 3] = (float) (marsVertices[marsIndices[i]]).getX();
			pMarsValues[i * 3 + 1] = (float) (marsVertices[marsIndices[i]]).getY();
			pMarsValues[i * 3 + 2] = (float) (marsVertices[marsIndices[i]]).getZ();
			tMarsValues[i * 2] = (float) (marsVertices[marsIndices[i]]).getS();
			tMarsValues[i * 2 + 1] = (float) (marsVertices[marsIndices[i]]).getT();
			nMarsValues[i * 3] = (float) (marsVertices[marsIndices[i]]).getNormalX();
			nMarsValues[i * 3 + 1] = (float) (marsVertices[marsIndices[i]]).getNormalY();
			nMarsValues[i * 3 + 2] = (float) (marsVertices[marsIndices[i]]).getNormalZ();
		}
		
		// Bind vertex buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[9]);
		FloatBuffer marsVertBuf = Buffers.newDirectFloatBuffer(pMarsValues);
		gl.glBufferData(GL_ARRAY_BUFFER, marsVertBuf.limit() * 4, marsVertBuf, GL_STATIC_DRAW);
		
		// Bind texture buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[10]);
		FloatBuffer marsTexBuf = Buffers.newDirectFloatBuffer(tMarsValues);
		gl.glBufferData(GL_ARRAY_BUFFER, marsTexBuf.limit() * 4, marsTexBuf, GL_STATIC_DRAW);
		
		// Bind normal buffer with a vbo entry.
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[11]);
		FloatBuffer marsNormalBuf = Buffers.newDirectFloatBuffer(nMarsValues);
		gl.glBufferData(GL_ARRAY_BUFFER, marsNormalBuf.limit() * 4, marsNormalBuf, GL_STATIC_DRAW);
		
		/*float[] cubePositions =
				{-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
						-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
						-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
						1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f};
		
		float[] pyramidPositions = {-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);*/
	}
	
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{
		float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0, 0, A);
		r.setElementAt(1, 1, q);
		r.setElementAt(2, 2, B);
		r.setElementAt(3, 2, -1.0f);
		r.setElementAt(2, 3, C);
		r.setElementAt(3, 3, 0.0f);
		return r;
	}
	
	public static void main(String[] args)
	{
		new Project2();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
	}
	
	public void dispose(GLAutoDrawable drawable)
	{
	}
	
	private int createShaderProgram()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		String vshaderSource[] = GLSLUtils.readShaderSource("shaders/vert.shader");
		String fshaderSource[] = GLSLUtils.readShaderSource("shaders/frag.shader");
		
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		
		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);
		
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}
	
	public Texture loadTexture(String textureFileName)
	{
		Texture tex = null;
		try
		{
			tex = TextureIO.newTexture(new File(textureFileName), false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return tex;
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
	
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getExtendedKeyCode();
		switch(keyCode)
		{
			case KeyEvent.VK_W:
				System.out.println("w");
				break;
			case KeyEvent.VK_S:
				System.out.println("s");
				break;
			case KeyEvent.VK_A:
				System.out.println("a");
				break;
			case KeyEvent.VK_D:
				System.out.println("d");
				break;
			case KeyEvent.VK_E:
				System.out.println("e");
				break;
			case KeyEvent.VK_Q:
				System.out.println("q");
				break;
			case KeyEvent.VK_LEFT:
				System.out.println("left");
				break;
			case KeyEvent.VK_RIGHT:
				System.out.println("right");
				break;
			case KeyEvent.VK_UP:
				System.out.println("up");
				break;
			case KeyEvent.VK_DOWN:
				System.out.println("down");
				break;
			case KeyEvent.VK_SPACE:
				System.out.println("space");
				break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
	
	}
}