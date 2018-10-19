package project2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
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
	private static final String EARTH_TEXTURE_FILE = "textures/earth.jpg";
	
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
	private float m_sphLocX, m_sphLocY, m_sphLocZ;
	private Sphere m_sun;
	private int earthTexture;
	private Texture joglEarthTexture;
	
	public Project2()
	{
		// Initialize default member variable values.
		m_vao = new int[1];
		m_vbo = new int[3];
		m_sun = new Sphere(24);
		
		// Set up JFrame properties.
		setTitle("Project 2 - 3D Modeling and Camera Manipulation");
		setSize(600, 600);
		m_myCanvas = new GLCanvas();
		m_myCanvas.addGLEventListener(this);
		m_myCanvas.addKeyListener(this);
		getContentPane().add(m_myCanvas);
		this.setVisible(true);
	}
	
	public void display(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glUseProgram(m_renderingProgram);
		
		int mvLoc = gl.glGetUniformLocation(m_renderingProgram, "mv_matrix");
		int projLoc = gl.glGetUniformLocation(m_renderingProgram, "proj_matrix");
		
		float aspect = (float) m_myCanvas.getWidth() / (float) m_myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		Matrix3D vMat = new Matrix3D();
		vMat.translate(-m_cameraX, -m_cameraY, -m_cameraZ);
		
		Matrix3D mMat = new Matrix3D();
		mMat.translate(m_sphLocX, m_sphLocY, m_sphLocZ);
		
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		int numVerts = m_sun.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		
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
		m_cameraX = 0.0f;
		m_cameraY = 0.0f;
		m_cameraZ = 2.0f;
		m_cubeLocX = 0.0f;
		m_cubeLocY = -2.0f;
		m_cubeLocZ = 0.0f;
		m_pyrLocX = 2.0f;
		m_pyrLocY = 2.0f;
		m_pyrLocZ = 0.0f;
		m_sphLocX = 0.0f;
		m_sphLocY = 0.0f;
		m_sphLocZ = -1.0f;
		
		joglEarthTexture = loadTexture(EARTH_TEXTURE_FILE);
		earthTexture = joglEarthTexture.getTextureObject();
	}
	
	private void setupVertices()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		Vertex3D[] vertices = m_sun.getVertices();
		int[] indices = m_sun.getIndices();
		
		float[] pValues = new float[indices.length * 3];
		float[] tValues = new float[indices.length * 2];
		float[] nValues = new float[indices.length * 3];
		
		for(int i = 0; i < indices.length; i++)
		{
			pValues[i * 3] = (float) (vertices[indices[i]]).getX();
			pValues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
			pValues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
			tValues[i * 2] = (float) (vertices[indices[i]]).getS();
			tValues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
			nValues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
			nValues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
			nValues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
		}
		
		float[] cubePositions =
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
		
		gl.glGenVertexArrays(m_vao.length, m_vao, 0);
		gl.glBindVertexArray(m_vao[0]);
		gl.glGenBuffers(m_vbo.length, m_vbo, 0);
		
		/*gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);*/
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[0]);
		FloatBuffer sphereVertBuf = Buffers.newDirectFloatBuffer(pValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereVertBuf.limit() * 4, sphereVertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[1]);
		FloatBuffer sphereTexBuf = Buffers.newDirectFloatBuffer(tValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereTexBuf.limit() * 4, sphereTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, m_vbo[2]);
		FloatBuffer sphereNormalBuf = Buffers.newDirectFloatBuffer(nValues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereNormalBuf.limit() * 4, sphereNormalBuf, GL_STATIC_DRAW);
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