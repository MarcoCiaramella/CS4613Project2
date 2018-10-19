package project2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vertex3D;
import graphicslib3D.shape.Sphere;

import javax.swing.*;
import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL4.*;

/**
 * Project 2: 3D Modeling and Camera Manipulation
 * <p>
 * Based on Program 4.3 - Multiple Models from Gordon & Clevenger.
 *
 * @author Eric Peterson
 */
public class Project2 extends JFrame implements GLEventListener
{
	/* **************** *
	 * Member Variables *
	 * **************** */
	private GLCanvas m_myCanvas;
	private int m_renderingProgram;
	private int m_vao[];
	private int m_vbo[];
	private float m_cameraX, m_cameraY, m_cameraZ;
	private float m_cubeLocX, m_cubeLocY, m_cubeLocZ;
	private float m_pyrLocX, m_pyrLocY, m_pyrLocZ;
	private Sphere m_sun;
	
	public Project2()
	{
		// Initialize default member variable values.
		m_vao = new int[1];
		m_vbo = new int[2];
		m_sun = new Sphere(24);
		
		// Set up JFrame properties.
		setTitle("Project 2 - 3D Modeling and Camera Manipulation");
		setSize(600, 600);
		m_myCanvas = new GLCanvas();
		m_myCanvas.addGLEventListener(this);
		getContentPane().add(m_myCanvas);
		this.setVisible(true);
	}
	
	public void display(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glUseProgram(m_renderingProgram);
		
		int mv_loc = gl.glGetUniformLocation(m_renderingProgram, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(m_renderingProgram, "proj_matrix");
		
		float aspect = (float) m_myCanvas.getWidth() / (float) m_myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		Matrix3D vMat = new Matrix3D();
		vMat.translate(-m_cameraX, -m_cameraY, -m_cameraZ);
		
		// ------------------- draw the cube using buffer #0
		Matrix3D mMat = new Matrix3D();
		mMat.translate(m_cubeLocX, m_cubeLocY, m_cubeLocZ);
		
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
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
		
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
	}
	
	public void init(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		m_renderingProgram = createShaderProgram();
		setupVertices();
		m_cameraX = 0.0f;
		m_cameraY = 0.0f;
		m_cameraZ = 8.0f;
		m_cubeLocX = 0.0f;
		m_cubeLocY = -2.0f;
		m_cubeLocZ = 0.0f;
		m_pyrLocX = 2.0f;
		m_pyrLocY = 2.0f;
		m_pyrLocZ = 0.0f;
	}
	
	private void setupVertices()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		Vertex3D[] vertices = m_sun.getVertices();
		int[] indices = m_sun.getIndices();
		
		float[] pvalues = new float[indices.length * 3];
		float[] tvalues = new float[indices.length * 2];
		float[] nvalues = new float[indices.length * 3];
		
		for(int i = 0; i < indices.length; i++)
		{
			pvalues[i * 3] = (float) (vertices[indices[i]]).getX();
			pvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
			pvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
			tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
			nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
			nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
		}
		
		float[] cube_positions =
				{-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
						-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
						-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
						1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f};
		
		float[] pyramid_positions = {-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};
		
		gl.glGenVertexArrays(m_vao.length, m_vao, 0);
		gl.glBindVertexArray(m_vao[0]);
		gl.glGenBuffers(m_vbo.length, m_vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cube_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf, GL_STATIC_DRAW);
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
}